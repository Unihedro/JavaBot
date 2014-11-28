package com.gmail.inverseconduit.chat;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.jsoup.Jsoup;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.datatype.*;
import com.gmail.inverseconduit.utils.PrintUtils;
import com.google.gson.Gson;

public class StackExchangeChat implements ChatInterface {

    private static final Logger                   LOGGER          = Logger.getLogger(StackExchangeChat.class.getName());

    private static final int                      MESSAGE_COUNT   = 5;

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

    @Override
    public boolean login(ProviderDescriptor descriptor, CredentialsProvider credentials) {
        HtmlPage loginPage = getLoginPage(descriptor);
        if (null == loginPage) { return false; }

        HtmlForm loginForm = processLoginPage(credentials.getIdentificator(), credentials.getAuthenticator(), loginPage);

        WebResponse response = submitLoginForm(loginForm);
        if (null == response) { return false; }

        loggedIn = (response.getStatusCode() == 200);
        logLoginMessage(descriptor.getDescription().toString(), credentials.getIdentificator(), response);
        return loggedIn;
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

    @Override
    public boolean leaveChat(ChatDescriptor descriptor) {
        //Let timeout take care of leave
        return chatMap.remove(descriptor) != null;
    }

    /**
     * Sends a plain text message to the chatroom specified
     * FIXME: clean up javadoc
     *
     * @param site
     *        the SESite the chatroom belongs to. (de-facto either
     *        STACK_OVERFLOW , STACK_EXCHANGE or META_STACK_EXCHANGE)
     * @param chatId
     *        the room number to post in. must be positive. If it isn't
     *        {@link IllegalArgumentException} will be thrown.
     * @param message
     *        The String to post into the chatroom. The string is not
     *        required to be encoded.
     * @return a boolean indicating the success of posting to this chat.
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
        if (message.length() >= 500 && message.length() < 600) {
            LOGGER.warning("Truncating message!");
            message = PrintUtils.truncate(message);
        }
        else if (message.length() > 500 && message.length() < 1000) {
            LOGGER.warning("Splitting message");
            String continuation = "..." + message.substring(message.length() / 2);
            message = message.substring(0, message.length() / 2) + "...";
            this.sender.schedule(() -> sendMessage(descriptor, continuation), 2, TimeUnit.SECONDS);
        }
        else if (message.length() >= 1000) {
            LOGGER.warning("Nobody sends messages this long!");
            message = message.substring(0, 495) + "...";
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
     * timestamps as maintained
     * internally
     */
    @Override
    public synchronized void queryMessages() {
        chatMap.keySet().forEach((descriptor) -> queryRoom(descriptor));
    }

    @Override
    public void broadcast(final String message) {
        chatMap.keySet().forEach((descriptor) -> sendMessage(descriptor, message));
    }

    private void queryRoom(final SeChatDescriptor descriptor) {
        String fkey = chatMap.get(descriptor).getElementById("fkey").getAttribute("value");
        String roomUrl = descriptor.buildRestRootUrl() + "events/";
        String rString = fetchJson(roomUrl, fkey);

        Gson gson = new Gson();
        JSONChatEvents events = gson.fromJson(rString, JSONChatEvents.class);

        if (null == events) { return; }
        events.setSite(SESite.fromUrl(descriptor.getProvider().getDescription().toString()));
        handleChatEvents(events);
    }

    private void handleInitialEvents(SeChatDescriptor descriptor, String fkey) {
        String restUrl = descriptor.buildRestRootUrl() + "events/";
        String rString = fetchJson(restUrl, fkey);
        LOGGER.info(rString);
        Gson gson = new Gson();
        JSONChatEvents assumeHandled = gson.fromJson(rString, JSONChatEvents.class);

        assumeHandled.getEvents().forEach(event -> handledMessages.add(event.getTime_stamp()));
    }

    private String fetchJson(final String roomUrl, final String fkey) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("fkey", fkey));
        params.add(new NameValuePair("mode", "messages"));
        params.add(new NameValuePair("msgCount", String.valueOf(MESSAGE_COUNT)));

        String rString;
        try {
            WebRequest r = new WebRequest(new URL(roomUrl), HttpMethod.POST);
            r.setRequestParameters(params);

            WebResponse response = webClient.loadWebResponse(r);
            rString = response.getContentAsString();

            LOGGER.finest("responseString: " + rString);
        } catch(IOException e1) {
            rString = "{\"events\": []}";
            LOGGER.severe("Exception when requesting events");
        }
        return rString;
    }

    private void handleChatEvents(final JSONChatEvents events) {
        events.getEvents().stream().filter(e -> e.getEvent_type() == ChatEventType.CHAT_MESSAGE && !handledMessages.contains(e.getTime_stamp())).forEach(event -> {
            String message = Jsoup.parse(event.getContent()).text();
            ChatMessage chatMessage = new ChatMessage(events.getSite(), event.getRoom_id(), event.getRoom_name(), event.getUser_name(), event.getUser_id(), message);
            LOGGER.finest("enqueueing message with timestamp: " + event.getTime_stamp());
            subscribers.forEach(s -> {
                try {
                    s.enqueueMessage(chatMessage);
                } catch(Exception e) {
                    LOGGER.warning("Could not enqueue message: " + message + "to subscriber " + s);
                }
            });
            handledMessages.add(event.getTime_stamp());
        });
    }

    @Override
    public void subscribe(final ChatWorker subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void unSubscribe(final ChatWorker subscriber) {
        subscribers.remove(subscriber);
    }

}
