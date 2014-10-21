// CLASS CREATED 2014/10/19 AT 4:41:58 P.M.
// JavaBot.java by Unihedron
package com.gmail.inverseconduit;

import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;
import com.gmail.inverseconduit.chat.StackExchangeChat;

import java.util.ArrayList;

/**
 * Procrastination: I'll fix this javadoc comment later.<br>
 * JavaBot @ com.gmail.inverseconduit
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
public class JavaBot {
    private StackExchangeChat seChat;
    private ArrayList<ChatMessageListener> listeners = new ArrayList<>();

    public JavaBot() {
        seChat = new StackExchangeChat(this);
    }

    public boolean addListener(ChatMessageListener listener) {
        return listeners.add(listener);
    }

    public boolean removeListener(ChatMessageListener listener) {
        return listeners.remove(listener);
    }

    public ArrayList<ChatMessageListener> getListeners() {
        return listeners;
    }

    public boolean login(SESite site, String username, String password) {
        return seChat.login(site, username, password);
    }

    public boolean joinChat(SESite site, int chatId) {
        return seChat.joinChat(site, chatId);
    }

    public boolean sendMessage(SESite site, int chatId, String message) {
        return seChat.sendMessage(site, chatId, message);
    }

    public void handleMessage(ChatMessage message) {
        System.out.println(message.toString());
        //sendMessage(message.getSite(), message.getRoomId(), "Test");
    }
}
