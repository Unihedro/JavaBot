package com.gmail.inverseconduit;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;
import org.w3c.dom.Node;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

public class StackExchangeBrowser {
    private static Logger logger = Logger.getLogger(StackExchangeBrowser.class.getName());
    private ArrayList<ChatMessageListener> messageListeners = new ArrayList<>();
    private boolean loggedIn = true;
    private WebClient webClient;

    public StackExchangeBrowser() {
        webClient = new WebClient(BrowserVersion.FIREFOX_24);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        //webClient.setAjaxController(new NicelyResynchronizingAjaxController());
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
            final String roomName = "Java";
            chatPage.addDomChangeListener(new DomChangeListener() {
                @Override
                public void nodeAdded(DomChangeEvent domChangeEvent) {
                    DomNode changedNode = domChangeEvent.getChangedNode();
                    Node classAttribute = changedNode.getAttributes().getNamedItem("class");

                    // This appears to be a chat message.
                    if(classAttribute != null && classAttribute.getTextContent().contains("user-container")) {
                        String userId = changedNode.getFirstChild().getAttributes().
                                getNamedItem("href").getTextContent().split("/")[1];

                        String username = changedNode.getFirstChild().getChildNodes().get(2).getTextContent().trim();

                        DomNodeList<DomNode> n = changedNode.getChildNodes().get(1).getChildNodes();
                        String message = n.get(n.getLength() - 1).getTextContent();

                        ChatMessage cMsg = new ChatMessage(site, chatId, roomName, username, userId, message);
                        for(ChatMessageListener listener : messageListeners) {
                            listener.onMessageReceived(cMsg);
                        }

                    }

                    System.out.println(changedNode.getAttributes().getNamedItem("class").getTextContent());
                }

                @Override
                public void nodeDeleted(DomChangeEvent domChangeEvent) {}
            });
            logger.info("Joined room.");
            while(true) {
                webClient.waitForBackgroundJavaScriptStartingBefore(10000);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
}
