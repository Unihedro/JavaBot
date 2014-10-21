// CLASS CREATED 2014/10/19 AT 4:41:58 P.M.
// JavaBot.java by Unihedron
package com.gmail.inverseconduit;

import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;

/**
 * Procrastination: I'll fix this javadoc comment later.<br>
 * JavaBot @ com.gmail.inverseconduit
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
public class JavaBot implements ChatMessageListener {
    private StackExchangeBrowser seBrowser;

    public JavaBot() {
        seBrowser = new StackExchangeBrowser();
        seBrowser.addMessageListener(this);
    }

    // Wrap StackExchangeBrowser methods for easier API access
    public boolean login(SESite site, String username, String password) {
        return seBrowser.login(site, username, password);
    }

    public boolean joinChat(SESite site, int chatId) {
        return seBrowser.joinChat(site, chatId);
    }

    public boolean sendMessage(SESite site, int chatId, String message) {
        return seBrowser.sendMessage(site, chatId, message);
    }

    @Override
    public void onMessageReceived(ChatMessage message) {
        System.out.println(message.toString());
        //sendMessage(message.getSite(), message.getRoomId(), "Test");
    }
}
