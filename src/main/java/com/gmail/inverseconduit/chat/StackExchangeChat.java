package com.gmail.inverseconduit.chat;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jsoup.Jsoup;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.datatype.ChatEventType;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.JSONChatEvents;
import com.google.gson.Gson;

public class StackExchangeChat implements ChatInterface {

    private static final Logger                               LOGGER          = Logger.getLogger(StackExchangeChat.class.getName());

    private static final int                                  MESSAGE_COUNT   = 5;

    private final EnumMap<SESite, HashMap<Integer, HtmlPage>> chatMap         = new EnumMap<>(SESite.class);

    private boolean                                           loggedIn        = true;

    private final WebClient                                   webClient;

    private final Set<ChatWorker>                             subscribers     = new HashSet<>();

    private final Set<Long>                                   handledMessages = new HashSet<>();

    public StackExchangeChat() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.setWebConnection(new WebConnectionWrapper(webClient));
    }

    @Override
    public boolean login(SESite site, String email, String password) {
        try {
            HtmlPage loginPage = webClient.getPage(new URL(site.getLoginUrl()));
            HtmlForm loginForm = loginPage.getFirstByXPath("//*[@id=\"se-login-form\"]");
            loginForm.getInputByName("email").setValueAttribute(email);
            loginForm.getInputByName("password").setValueAttribute(password);
            WebResponse response = loginForm.getInputByName("submit-button").click().getWebResponse();
            loggedIn = (response.getStatusCode() == 200);

            String logMessage;
            if (loggedIn) {
                logMessage = String.format("Logged in to %s with email %s", site.getRootUrl(), email);
            }
            else {
                logMessage = String.format("Login failed. Got status code %d", response.getStatusCode());
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
    public boolean joinChat(SESite site, int chatId) {
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
            // jsonChatConnection.setEnabled(true);
            addChatPage(site, chatId, chatPage);
            LOGGER.info("Joined room.");
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        sendMessage(site, chatId, "*~JavaBot, at your service*");
        return true;
    }

    private void addChatPage(SESite site, int id, HtmlPage page) {
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
    public synchronized boolean sendMessage(SESite site, int chatId, String message) {
        if (0 >= chatId) { throw new IllegalArgumentException("Room number must be a positive number"); }

        HashMap<Integer, HtmlPage> map = chatMap.get(site);
        HtmlPage page = map.get(chatId);
        if (null == page)
            return false;
        String fkey = page.getElementById("fkey").getAttribute("value");

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("fkey", fkey));
        params.add(new NameValuePair("text", message));

        try {
            WebRequest r = new WebRequest(new URL(String.format("http://chat.%s.com/chats/%d/messages/new", site.getDir(), chatId)), HttpMethod.POST);
            r.setRequestParameters(params);
            WebResponse response = webClient.loadWebResponse(r);
            if (response.getStatusCode() != 200) {
                LOGGER.warning(String.format("Could not send message. Response(%d): %s", response.getStatusCode(), response.getStatusMessage()));
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
     * Queries the 5 latest messages for a given chatroom and enqueues them to
     * the javaBot, respecting the already handled timestamps as maintained
     * internally
     * 
     * @param site
     *        The SESite the room to query belongs to.
     * @param chatId
     *        The room number of the room to query. It must be positive. If
     *        it isn't {@link IllegalArgumentException} will be thrown.
     */
    @Override
    public synchronized void queryMessages(SESite site, int chatId) {
        if (0 >= chatId) { throw new IllegalArgumentException("Room number must be positive"); }

        HashMap<Integer, HtmlPage> map = chatMap.get(site);
        HtmlPage page = map.get(chatId);
        if (page == null)
            return;
        String fkey = page.getElementById("fkey").getAttribute("value");

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

        Gson gson = new Gson();
        JSONChatEvents events = gson.fromJson(rString, JSONChatEvents.class);
        events.setSite(site);
        handleChatEvents(events);
    }

    private void handleChatEvents(JSONChatEvents events) {
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
    public void subscribe(ChatWorker subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void unSubscribe(ChatWorker subscriber) {
        subscribers.remove(subscriber);
    }
}
