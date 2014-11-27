package com.gmail.inverseconduit.chat;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
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
import com.gmail.inverseconduit.datatype.ChatDescriptor;
import com.gmail.inverseconduit.datatype.ChatEventType;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.CredentialsProvider;
import com.gmail.inverseconduit.datatype.JSONChatEvents;
import com.gmail.inverseconduit.datatype.ProviderDescriptor;
import com.gmail.inverseconduit.utils.PrintUtils;
import com.google.gson.Gson;

public class StackExchangeChat implements ChatInterface {

    @FunctionalInterface
    interface AllRoomsAction {

        void accept(SESite site, Integer chatId, String fkey);
    }

    private static final Logger                               LOGGER          = Logger.getLogger(StackExchangeChat.class.getName());

    private static final int                                  MESSAGE_COUNT   = 5;

    private final EnumMap<SESite, HashMap<Integer, HtmlPage>> chatMap         = new EnumMap<>(SESite.class);

    private boolean                                           loggedIn        = true;

    private final WebClient                                   webClient;

    private final Set<ChatWorker>                             subscribers     = new HashSet<>();

    //TODO: Change that from timestamp-handling to id-based handling or move it to the ChatWorker
    private final Set<Long>                                   handledMessages = new HashSet<>();

    private final ScheduledThreadPoolExecutor                 sender          = new ScheduledThreadPoolExecutor(1);

    public StackExchangeChat() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.setWebConnection(new WebConnectionWrapper(webClient));
    }

    @Override
    public boolean login(
        final SESite site, final String email, final String password) {
        try {
            HtmlPage loginPage = webClient.getPage(new URL(site.getLoginUrl()));
            HtmlForm loginForm =
                    loginPage.getFirstByXPath("//*[@id=\"se-login-form\"]");
            loginForm.getInputByName("email").setValueAttribute(email);
            loginForm.getInputByName("password").setValueAttribute(password);
            WebResponse response =
                    loginForm.getInputByName("submit-button").click().getWebResponse();
            loggedIn = (response.getStatusCode() == 200);

            String logMessage;
            if (loggedIn) {
                logMessage =
                        String.format("Logged in to %s with email %s", site.getRootUrl(), email);
            }
            else {
                logMessage =
                        String.format("Login failed. Got status code %d", response.getStatusCode());
            }
            LOGGER.info(logMessage);

            return loggedIn;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public boolean joinChat(final SESite site, final int chatId) {
        if ( !loggedIn) {
            LOGGER.warning("Not logged in. Cannot join chat.");
            return false;
        }
        if (chatMap.containsKey(site) && chatMap.get(site).containsKey(chatId)) {
            LOGGER.warning("Already in that room.");
            return false;
        }
        try {
            // TODO new rooms require new windows
            webClient.waitForBackgroundJavaScriptStartingBefore(10000);
            HtmlPage chatPage = webClient.getPage(site.urlToRoom(chatId));
            handleInitialEvents(site, chatId, chatPage.getElementById("fkey").getAttribute("value"));
            addChatPage(site, chatId, chatPage);
            LOGGER.info("Joined room.");
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        sendMessage(site, chatId, "*~JavaBot, at your service*");
        return true;
    }

    private void handleInitialEvents(SESite site, int chatId, String fkey) {
        String rString = fetchJson(site, chatId, fkey);
        Gson gson = new Gson();
        JSONChatEvents assumeHandled = gson.fromJson(rString, JSONChatEvents.class);
        assumeHandled.getEvents().forEach(event -> handledMessages.add(event.getTime_stamp()));
    }

    @Override
    public boolean leaveChat(final SESite site, final int chatId) {
        //Let timeout take care of leave
        return chatMap.get(site).remove(chatId) != null;
    }

    private void addChatPage(final SESite site, final int id, final HtmlPage page) {
        HashMap<Integer, HtmlPage> siteMap = chatMap.get(site);
        if (null == siteMap)
            siteMap = new HashMap<>();
        siteMap.put(id, page);
        chatMap.put(site, siteMap);
    }

    /**
     * Sends a plain text message to the chatroom specified
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
    public synchronized boolean sendMessage(final SESite site, final int chatId, String message) {
        if (0 >= chatId) { throw new IllegalArgumentException("Room number must be a positive number"); }
        if (message.length() >= 500 && message.length() < 600) {
            LOGGER.warning("Truncating message!");
            message = PrintUtils.truncate(message);
        }
        else if (message.length() > 500 && message.length() < 1000) {
            LOGGER.warning("Splitting message");
            String continuation = "..." + message.substring(message.length() / 2);
            message = message.substring(0, message.length() / 2) + "...";
            this.sender.schedule(() -> sendMessage(site, chatId, continuation), 2, TimeUnit.SECONDS);
        }
        else if (message.length() >= 1000) {
            LOGGER.warning("Nobody sends messages this long!");
            message = message.substring(0, 495) + "...";
        }

        HashMap<Integer, HtmlPage> map = chatMap.get(site);
        HtmlPage page = map.get(chatId);
        if (null == page)
            return false;
        String fkey = page.getElementById("fkey").getAttribute("value");

        return sendMessage(site, chatId, fkey, message);
    }

    private boolean sendMessage(final SESite site, final int chatId, final String fkey, final String message) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("fkey", fkey));
        params.add(new NameValuePair("text", message));

        try {
            URL newMessageUrl = new URL(String.format("http://chat.%s.com/chats/%d/messages/new", site.getDir(), chatId));
            WebRequest r = new WebRequest(newMessageUrl, HttpMethod.POST);
            r.setRequestParameters(params);
            WebResponse response = webClient.loadWebResponse(r);
            if (response.getStatusCode() != 200) {
                LOGGER.warning(String.format("Could not send message. Response(%d): %s", response.getStatusCode(), response.getStatusMessage()));
                LOGGER.warning("Posted against URL: " + newMessageUrl);
                this.sender.schedule(() -> sendMessage(site, chatId, fkey, message), 5, TimeUnit.SECONDS);
                return false;
            }
            // TODO: "You must login to post" message also returns statuscode
            // 200!

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
        forAllRooms((site, chatId, page) -> queryRoom(site, chatId, page));
    }

    @Override
    public void broadcast(final String message) {
        forAllRooms((site, chatId, fkey) -> sendMessage(site, chatId, fkey, message));
    }

    private void forAllRooms(AllRoomsAction action) {
        chatMap.forEach((site, chatRooms) -> {
            chatRooms.forEach((chatId, page) -> action.accept(site, chatId, page.getElementById("fkey").getAttribute("value")));
        });
    }

    private void queryRoom(final SESite site, final Integer chatId, final String fkey) {
        String rString = fetchJson(site, chatId, fkey);
        Gson gson = new Gson();
        JSONChatEvents events = gson.fromJson(rString, JSONChatEvents.class);
        if (null == events) { return; }
        events.setSite(site);
        handleChatEvents(events);
    }

    private String fetchJson(final SESite site, final Integer chatId, final String fkey) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("fkey", fkey));
        params.add(new NameValuePair("mode", "messages"));
        params.add(new NameValuePair("msgCount", String.valueOf(MESSAGE_COUNT)));

        String rString;
        try {
            WebRequest r = new WebRequest(new URL(String.format("http://chat.%s.com/chats/%d/events", site.getDir(), chatId)), HttpMethod.POST);
            r.setRequestParameters(params);

            WebResponse response = webClient.loadWebResponse(r);
            rString = response.getContentAsString();

            LOGGER.finest("responseString: " + rString);
        } catch(IOException e1) {
            rString = "{}";
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

    //FIXME: Implement these methods properly!
    @Override
    public boolean sendMessage(ChatDescriptor descriptor, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean joinChat(ChatDescriptor descriptor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean leaveChat(ChatDescriptor descriptor) {
        throw new UnsupportedOperationException();
    }

}
