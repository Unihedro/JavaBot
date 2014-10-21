package com.gmail.inverseconduit;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gmail.inverseconduit.chat.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StackExchangeBrowser {
    private static Logger logger = Logger.getLogger(StackExchangeBrowser.class.getName());
    private ArrayList<ChatMessageListener> messageListeners = new ArrayList<>();
    private boolean loggedIn = true;
    private WebClient webClient;
    private JSONChatConnection jsonChatConnection;

    public StackExchangeBrowser() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter").setLevel(Level.OFF);
        Logger.getLogger("com.gargoylesoftware.htmlunit.DefaultCssErrorHandler").setLevel(Level.OFF);
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
            if(response.getStatusCode() == 200) {
                logger.info(String.format("Logged in to %s with email %s", site.getRootUrl(), email));
                loggedIn = true;
            }
            else {
                logger.info(String.format("Login failed. Got status code %d", response.getStatusCode()));
            }
            return true;
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
        try {
            webClient.waitForBackgroundJavaScriptStartingBefore(30000);
            HtmlPage chatPage = webClient.getPage(site.urlToRoom(chatId));
            jsonChatConnection.setEnabled(true);
            logger.info("Joined room.");
            while(true) {
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public ArrayList<ChatMessageListener> getMessageListeners() {
        return messageListeners;
    }

    public void addMessageListener(ChatMessageListener listener) {
        messageListeners.add(listener);
    }

    public boolean removeMessageListener(ChatMessageListener listener) {
        return messageListeners.remove(listener);
    }

    public void handleChatEvents(JSONChatEvents events) {
        for(JSONChatEvent event : events.getEvents()) {
            switch(event.getEvent_type()) {
                case ChatEventType.CHAT_MESSAGE:
                    ChatMessage chatMessage = new ChatMessage(
                            events.getSite(), event.getRoom_id(), event.getRoom_name(),
                            event.getUser_name(), event.getUser_id(), event.getContent());
                    for(ChatMessageListener listener : messageListeners) {
                        listener.onMessageReceived(chatMessage);
                    }
                    break;
            }
        }
    }
}
