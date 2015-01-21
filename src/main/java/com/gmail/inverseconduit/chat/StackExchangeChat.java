package com.gmail.inverseconduit.chat;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.datatype.ChatDescriptor;
import com.gmail.inverseconduit.datatype.ChatEventType;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.CredentialsProvider;
import com.gmail.inverseconduit.datatype.JSONChatEvents;
import com.gmail.inverseconduit.datatype.ProviderDescriptor;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;
import com.gmail.inverseconduit.utils.PrintUtils;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

public class StackExchangeChat implements ChatInterface {

    private static final Logger                   LOGGER          = Logger.getLogger(StackExchangeChat.class.getName());

    private static final int                      MESSAGE_COUNT   = 5;

    private static final Gson                     GSON            = new Gson();

    private final Map<SeChatDescriptor, HtmlPage> chatMap         = new TreeMap<>();

    private boolean                               loggedIn        = true;

    private final WebClient                       webClient;

    private final Set<ChatWorker>                 subscribers     = new HashSet<>();

    //TODO: Change that from timestamp-handling to id-based handling or move it to the ChatWorker
    private final Set<Long>                       handledMessages = new HashSet<>();

    private final ScheduledThreadPoolExecutor     sender          = new ScheduledThreadPoolExecutor(1);

    public StackExchangeChat() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.setWebConnection(new WebConnectionWrapper(webClient));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean login(ProviderDescriptor descriptor, CredentialsProvider credentials) {
        if (descriptor.getDescription().toString().matches("(?!.*?(?:stackoverflow|meta)).+"))
            loggedIn = openIdLogin(credentials.getIdentificator(), credentials.getAuthenticator());
        else {
            HtmlPage loginPage = getLoginPage(descriptor);
            if (null == loginPage) { return false; }

            HtmlForm loginForm = processLoginPage(credentials.getIdentificator(), credentials.getAuthenticator(), loginPage);

            WebResponse response = submitLoginForm(loginForm);
            if (null == response) { return false; }

            loggedIn = (response.getStatusCode() == 200);
            logLoginMessage(descriptor.getDescription().toString(), credentials.getIdentificator(), response);
        }
        return loggedIn;
    }

    public boolean openIdLogin(String email, String password) {
        try {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            { // Log into Stack Exchange.
                WebResponse getRes = webClient.loadWebResponse(new WebRequest(
                    UrlUtils.toUrlUnsafe("https://openid.stackexchange.com/account/login"),
                    HttpMethod.GET
                ));
                if (getRes == null)
                    // throw new IllegalArgumentException("Could not get OpenID fkey.");
                    return false;
                String response = getRes.getContentAsString();
                LOGGER.info("Got response from fetching fkey of OpenID login:" + response);

                WebRequest post = new WebRequest(
                    UrlUtils.toUrlUnsafe("https://openid.stackexchange.com/account/login/submit"),
                    HttpMethod.POST
                );
                try {
                    post.setRequestParameters(ImmutableList.of(
                        new NameValuePair("email", email),
                        new NameValuePair("password", password),
                        new NameValuePair("fkey",
                            builder.parse(getRes.getContentAsStream())
                                .getElementById("fkey")
                                .getAttribute("value")
                        )
                    ));
                } catch(SAXException die) {
                    // throw new IllegalStateException("Parsing response text as DOM has caused a parse exception.", die);
                    return false;
                }
                WebResponse postRes = webClient.loadWebResponse(post);
                if (null == postRes || !Strings.isNullOrEmpty(postRes.getResponseHeaderValue("p3p")))
                    // throw new IllegalArgumentException("Could not post to submit of OpenID.");
                    return false;
            }

            { // Log into Stack Exchange Chat.
                WebResponse getRes = webClient.loadWebResponse(new WebRequest(
                    UrlUtils.toUrlUnsafe("http://stackexchange.com/users/chat-login"),
                    HttpMethod.GET
                ));
                WebRequest post = new WebRequest(
                    UrlUtils.toUrlUnsafe("http://chat.stackexchange.com/users/login/global"),
                    HttpMethod.POST
                );
                try {
                    Document dom = builder.parse(getRes.getContentAsStream());
                    post.setRequestParameters(ImmutableList.of(
                        new NameValuePair("authToken", dom.getElementById("authToken").getAttribute("value")),
                        new NameValuePair("nonce", dom.getElementById("nonce").getAttribute("value"))
                    ));
                } catch(SAXException die) {
                    // throw new IllegalStateException("Parsing response text as DOM has caused a parse exception.", die);
                    return false;
                }
                post.setAdditionalHeaders(ImmutableMap.of(
                    "Origin", "http://chat.stackexchange.com",
                    "Referer", "http://chat.stackexchange.com"
                ));
                WebResponse postRes = webClient.loadWebResponse(post);
                String response = postRes.getContentAsString();
                return response.contains("Welcome");
            }
        } catch (ParserConfigurationException e) {
            // Ignore - virtually impossible.
        } catch(IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private WebResponse submitLoginForm(HtmlForm loginForm) {
        WebResponse response;
        try {
            response = loginForm.getInputByName("submit-button").click().getWebResponse();
        } catch(ElementNotFoundException | IOException e) {
            LOGGER.severe("Couldn't find submit button to Form / IOException when logging in");
            e.printStackTrace();
            return null;
        }
        return response;
    }

    private HtmlPage getLoginPage(ProviderDescriptor descriptor) {
        HtmlPage loginPage;
        try {
            loginPage = webClient.getPage(new URL(descriptor.getDescription().toString().replace("chat", "www") + "users/login"));
        } catch(FailingHttpStatusCodeException | IOException e) {
            LOGGER.severe("Couldn't fetch Login Page / IOException when logging in");
            e.printStackTrace();
            return null;
        }
        return loginPage;
    }

    private void logLoginMessage(final String site, final String email, WebResponse response) {
        String logMessage;
        if (loggedIn) {
            logMessage = String.format("Logged in to %s with email %s", site, email);
        }
        else {
            logMessage = String.format("Login failed. Got status code %d", response.getStatusCode());
        }
        LOGGER.info(logMessage);
    }

    private HtmlForm processLoginPage(final String email, final String password, HtmlPage loginPage) {
        HtmlForm loginForm = loginPage.getFirstByXPath("//*[@id=\"se-login-form\"]");
        loginForm.getInputByName("email").setValueAttribute(email);
        loginForm.getInputByName("password").setValueAttribute(password);
        return loginForm;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean joinChat(ChatDescriptor descriptor) {
        if ( ! (descriptor instanceof SeChatDescriptor)) {
            LOGGER.warning("Passed descriptor was not suitable for describing an Se Chat");
            return false;
        }
        if ( !loggedIn) {
            LOGGER.warning("Not logged in. Cannot join chat.");
            return false;
        }
        SeChatDescriptor seDescriptor = (SeChatDescriptor) descriptor;

        if (chatMap.containsKey(seDescriptor)) {
            LOGGER.warning("Already in that room.");
            return false;
        }

        webClient.waitForBackgroundJavaScriptStartingBefore(10000);
        HtmlPage chatPage;
        try {
            //FIXME: get room description sucks
            chatPage = webClient.getPage(seDescriptor.buildRoomUrl());
            chatMap.put(seDescriptor, chatPage);
        } catch(FailingHttpStatusCodeException | IOException e) {
            e.printStackTrace();
            return false;
        }
        handleInitialEvents(seDescriptor, chatPage.getElementById("fkey").getAttribute("value"));
        sendMessage(seDescriptor, "*~JavaBot at your service*");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean leaveChat(ChatDescriptor descriptor) {
        //Let timeout take care of leave
        return chatMap.remove(descriptor) != null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean sendMessage(ChatDescriptor descriptor, String message) {
        LOGGER.info("entering sendMessage with descriptor: " + descriptor);
        if ( ! (descriptor instanceof SeChatDescriptor)) {
            LOGGER.warning("descriptor was not suitable to describe an SeChat");
            return false;
        }
        SeChatDescriptor seDescriptor = (SeChatDescriptor) descriptor;
        message = handleMessageOversize(seDescriptor, message);
        String fkey = chatMap.get(seDescriptor).getElementById("fkey").getAttribute("value");
        return sendMessage(seDescriptor.buildRestRootUrl(), fkey, message);
    }

    private String handleMessageOversize(final SeChatDescriptor descriptor, String message) {
        if (message.length() >= 500) {
            LOGGER.warning("Truncating message!");
            List<String> messageTokens = PrintUtils.splitUsefully(message);
            LOGGER.finest("Message tokens are: " + messageTokens);

            StringBuilder messageBuilder = new StringBuilder();
            for (String token : messageTokens) {
                if (messageBuilder.length() + token.length() < 495) {
                    messageBuilder.append(' ').append(token);
                }
                else {
                    LOGGER.info("Message split part: " + messageBuilder.toString());
                    sendMessage(descriptor.buildRestRootUrl(), chatMap.get(descriptor).getElementById("fkey").getAttribute("value"), messageBuilder.toString());
                    messageBuilder = new StringBuilder(token);
                }
            }

            if (messageBuilder.length() < 12) { return ""; }
            return messageBuilder.toString();
        }
        return message;
    }

    private boolean sendMessage(String restRootUrl, String fkey, String message) {
        LOGGER.info("Sending message: " + message);
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("fkey", fkey));
        params.add(new NameValuePair("text", message));

        try {
            URL newMessageUrl = new URL(restRootUrl + "/messages/new");
            WebRequest r = new WebRequest(newMessageUrl, HttpMethod.POST);
            r.setRequestParameters(params);
            WebResponse response = webClient.loadWebResponse(r);
            if (response.getStatusCode() != 200) {
                LOGGER.warning(String.format("Could not send message. Response(%d): %s", response.getStatusCode(), response.getStatusMessage()));
                LOGGER.warning("Posted against URL: " + newMessageUrl);
                //FIXME retry at a more random time...
                this.sender.schedule(() -> sendMessage(restRootUrl, fkey, message), 5, TimeUnit.SECONDS);
                return false;
            }
            //TODO: "You must log in to post also returns HTTP 200
            LOGGER.info("POST " + r.toString());
            return true;
        } catch(IOException e) {
            LOGGER.warning("Couldn't send message due to IOException");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Queries the 5 latest messages for all chatrooms and enqueues them to
     * the subscribed {@link ChatWorker Workers}, respecting the already handled
     * timestamps as maintained internally.
     *
     * @see ChatInterface#queryMessages()
     */
    @Override
    public synchronized void queryMessages() {
        chatMap.entrySet().forEach((entry) -> {
            queryRoomEvents(entry.getKey(), entry.getValue().getElementById("fkey").getAttribute("value"));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void broadcast(final String message) {
        chatMap.keySet().forEach((descriptor) -> sendMessage(descriptor, message));
    }

    private void queryRoomEvents(final SeChatDescriptor descriptor, final String fkey) {
        JSONChatEvents events = queryForMessages(descriptor, fkey);
        if (null == events) { return; }

        events.setSite(SESite.fromUrl(descriptor.getProvider().getDescription().toString()));
        handleChatEvents(events);
    }

    private JSONChatEvents queryForMessages(final SeChatDescriptor descriptor, final String fkey) {
        String roomUrl = descriptor.buildRestRootUrl() + "events/";
        String rString = fetchMessageJson(roomUrl, fkey);

        JSONChatEvents events = GSON.fromJson(rString, JSONChatEvents.class);
        return events;
    }

    private void handleInitialEvents(SeChatDescriptor descriptor, String fkey) {
        JSONChatEvents assumeHandled = queryForMessages(descriptor, fkey);
        if (null == assumeHandled) { return; }

        assumeHandled.getEvents().forEach(event -> handledMessages.add((long) event.getMessage_id()));
    }

    private String fetchMessageJson(final String roomUrl, final String fkey) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("fkey", fkey));
        params.add(new NameValuePair("mode", "messages"));
        params.add(new NameValuePair("msgCount", String.valueOf(MESSAGE_COUNT)));

        return fetchJson(roomUrl, params);
    }

    private String fetchJson(final String restUrl, final ArrayList<NameValuePair> params) {
        String rString;
        try {
            WebRequest r = new WebRequest(new URL(restUrl), HttpMethod.POST);
            r.setRequestParameters(params);

            WebResponse response = webClient.loadWebResponse(r);
            rString = response.getContentAsString();
            return rString;
        } catch(IOException e) {
            LOGGER.warning("Exception when fetching Json from: " + restUrl);
            e.printStackTrace();
            return "";
        }
    }

    private void handleChatEvents(final JSONChatEvents events) {
        //@formatter:off
        events.getEvents().stream()
            .filter(e -> e.getEvent_type() == ChatEventType.CHAT_MESSAGE && !handledMessages.contains((long) e.getMessage_id()))
            .map(event -> ChatMessage.fromJsonChatEvent(event, events.getSite()))
            .forEach(message -> {
                subscribers.forEach(s -> {
                    try {
                        s.enqueueMessage(message);
                    } catch(Exception e) {
                        LOGGER.warning("Could not enqueue message: " + message + "to subscriber " + s);
                    }
                });
                handledMessages.add(message.getMessageId());
            });
        //@formatter:on
    }

    @Override
    public void subscribe(final ChatWorker subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void unSubscribe(final ChatWorker subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void close() throws Exception {
        subscribers.clear();
        chatMap.clear();
        sender.shutdown();
        webClient.closeAllWindows();
    }

    @Override
    public Collection<ChatWorker> getSubscriptions() {
        return Collections.unmodifiableCollection(subscribers);
    }

}
