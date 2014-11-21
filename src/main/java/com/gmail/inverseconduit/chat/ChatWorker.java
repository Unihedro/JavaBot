package com.gmail.inverseconduit.chat;

import javax.annotation.concurrent.ThreadSafe;

import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * Interface to specify classes that work on the basis of chatMessages.
 * These classes must be implemented Threadsafe, as calls to their methods may be performed by any thread.
 * 
 * @author vogel612<<a href="vogel612@gmx.de">vogel612@gmx.de</a>>
 */
@ThreadSafe
public interface ChatWorker {

    void processMessages();

    boolean enqueueMessage(ChatMessage chatMessage) throws InterruptedException;

}
