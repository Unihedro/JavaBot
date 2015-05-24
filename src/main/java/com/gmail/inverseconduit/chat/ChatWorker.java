package com.gmail.inverseconduit.chat;

import javax.annotation.concurrent.ThreadSafe;

import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * Interface to specify classes that work on the basis of chatMessages.
 * These classes must be implemented Threadsafe, as calls to their methods may
 * be performed by any thread.
 * 
 * @author vogel612<<a href="vogel612@gmx.de">vogel612@gmx.de</a>>
 */
@ThreadSafe
public interface ChatWorker {

    /**
     * ChatMessage instance that can be used as poison pill to shut down
     * ChatWorkers
     */
    public static final ChatMessage POISON_PILL = new ChatMessage(null, -1, "", "", -1, "", -1);

    /**
     * Performs any specific tasks that need to be done before enqueued messages
     * can be processed.</br>
     * This also triggers the processing of messages. Callers are strongly
     * encouraged to call start before begginning to enqueue messages.</br></br>
     * Subsequent calls to this method should be safe, but are not guaranteed to
     * be. The safety of subsequent calls is up to the implementing class.
     * </br>
     * It is expected to be called at least once in the lifecycle of a
     * ChatWorker.
     */
    void start();

    /**
     * Enqueues a message to be processed. null messages <i>may</i> be processed
     * at the discretion of the implementing class.
     * Enqueuing the {@link #POISON_PILL} signals the ChatWorker to not expect
     * any further input and to cease operation.
     * Depending on the implementation, a subsequent call to {@link #start()}
     * can "revive" the ChatWorker
     * 
     * @param chatMessage
     *        the chat message requiring processing.
     * @return success or failure status of the enqueueing operation.
     * @throws InterruptedException
     */
    boolean enqueueMessage(ChatMessage chatMessage) throws InterruptedException;

}
