package com.gmail.inverseconduit.chat;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.SESite;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StackExchangeChat {
    private final static Logger logger = Logger.getLogger(StackExchangeChat.class.getName());
    private final EnumMap<SESite, HashMap<Integer, HtmlPage>> chatMap = new EnumMap<>(SESite.class);
    private boolean loggedIn = true;
    private final WebClient webClient;
    private final JSONChatConnection jsonChatConnection;
    private final JavaBot javaBot;

    public StackExchangeChat(JavaBot javaBot) {
        this.javaBot = javaBot;
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter").setLevel(Level.OFF);
        Logger.getLogger("com.gargoylesoftware.htmlunit.DefaultCssErrorHandler").setLevel(Level.OFF);
        Logger.getLogger("com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl").setLevel(Level.OFF);
        Logger.getLogger("com.gargoylesoftware.htmlunit.html.InputElementFactory").setLevel(Level.OFF);
        jsonChatConnection = new JSONChatConnection(webClient, this);
        webClient.setWebConnection(jsonChatConnection);
    }

    public boolean login(SESite site, String email, String password) {
        try {
            HtmlPage loginPage = webClient.getPage(new URL(site.getLoginUrl()));
            HtmlForm loginForm = loginPage.getFirstByXPath("//*[@id=\"se-login-form\"]");
            loginForm.getInputByName("email").setValueAttribute(email);
            loginForm.getInputByName("password").setValueAttribute(password);
            WebResponse response = loginForm.getInputByName("submit-button").click().getWebResponse();
            loggedIn = (response.getStatusCode() == 200);
            
            String logMessage;
            if(loggedIn) {
                logMessage = String.format("Logged in to %s with email %s", site.getRootUrl(), email);
            } else {
                logMessage = String.format("Login failed. Got status code %d", response.getStatusCode());
            }
            logger.info(logMessage);
            
            return loggedIn;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean joinChat(SESite site, int chatId) {
        if(!loggedIn) {
            logger.warning("Not logged in. Cannot join chat.");
            return false;
        }
        if(chatMap.containsKey(site) && chatMap.get(site).containsKey(chatId)) {
            logger.warning("Already in that room.");
            return false;
        }
        try {
            // TODO new rooms require new windows
            webClient.waitForBackgroundJavaScriptStartingBefore(10000);
            HtmlPage chatPage = webClient.getPage(site.urlToRoom(chatId));
            jsonChatConnection.setEnabled(true);
            addChatPage(site, chatId, chatPage);
            logger.info("Joined room.");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void addChatPage(SESite site, int id, HtmlPage page) {
        HashMap<Integer, HtmlPage> siteMap = chatMap.get(site);
        if(siteMap == null) {
            siteMap = new HashMap<>();
        }
        siteMap.put(id, page);
        chatMap.put(site, siteMap);
    }

    protected void handleChatEvents(JSONChatEvents events) {
        for(JSONChatEvent event : events.getEvents()) {
            switch(event.getEvent_type()) {
                case ChatEventType.CHAT_MESSAGE:
                    String message = Jsoup.parse(event.getContent()).text();
                    ChatMessage chatMessage = new ChatMessage(
                            events.getSite(), event.getRoom_id(), event.getRoom_name(),
                            event.getUser_name(), event.getUser_id(), message);
                    javaBot.queueMessage(chatMessage);
                    break;
            }
        }
    }

    public synchronized boolean sendMessage(SESite site, int chatId, String message) {
        try {
            HashMap<Integer, HtmlPage> map = chatMap.get(site);
            HtmlPage page = map.get(chatId);
            if (page == null) return false;
            String fkey = page.getElementById("fkey").getAttribute("value");
            WebRequest r = new WebRequest(
                    new URL(String.format("http://chat.%s.com/chats/%d/messages/new", site.getDir(), chatId)),
                    HttpMethod.POST);
            ArrayList<NameValuePair> params = new ArrayList<>();
            params.add(new NameValuePair("fkey", fkey));
            params.add(new NameValuePair("text", message));
            r.setRequestParameters(params);
            WebResponse response = webClient.loadWebResponse(r);
            if(response.getStatusCode() != 200) {
                logger.warning(String.format("Could not send message. Response(%d): %s",
                        response.getStatusCode(), response.getStatusMessage()));
                return false;
            }
            logger.info("POST " + r.toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
