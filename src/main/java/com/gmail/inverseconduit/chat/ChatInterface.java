package com.gmail.inverseconduit.chat;

import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.bot.AbstractBot;

/**
 * Interface specifying least common methods for a class that acts as interface
 * to a chat.
 * The Instances of this class are supposed to be threadsafe.
 * A ChatInterface currently produces
 * {@link com.gmail.inverseconduit.datatype.ChatMessage ChatMessages},
 * subscribers must implement {@link AbstractBot}
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
public interface ChatInterface extends Subscribable<AbstractBot> {

    public void queryMessages(SESite site, int chatId);

    public boolean sendMessage(SESite site, int chatId, String message);
}
