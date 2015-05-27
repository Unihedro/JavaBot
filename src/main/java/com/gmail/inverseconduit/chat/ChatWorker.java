package com.gmail.inverseconduit.chat;

import javax.annotation.concurrent.ThreadSafe;

import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * Interface to specify classes that work on the basis of {@link ChatMessage
 * ChatMessages}.
 * 
 * @apiNote These classes must be implemented Threadsafe, as calls to their
 *          methods may
 *          be performed by any thread.
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
     * <p>
     * Performs any specific tasks that need to be done before enqueued messages
     * can be processed.
     * </p>
     * <p>
     * Subsequent calls to this method should be safe, but are not guaranteed to
     * be. The safety of subsequent calls is up to the implementing class.
     * </p>
     * 
     * @apiNote It is expected to be called at least once in the lifecycle of a
     *          ChatWorker. This also triggers the processing of messages.
     *          Callers are
     *          strongly encouraged to call start before begginning to enqueue
     *          messages
     */
    void start();

    /**
     * <p>
     * Enqueues a message to be processed. <tt>null</tt> messages
     * <b><i>may</i></b> be processed at the discretion of the implementing
     * class.
     * </p>
     * <p>
     * Enqueuing the {@link #POISON_PILL} signals the ChatWorker to not expect
     * any further input and to cease operation in preparation for shutdown.
     * </p>
     * 
     * @implNote Depending on the implementation, a subsequent call to
     *           {@link #start()} can "revive" the ChatWorker
     * @param chatMessage
     *        the chat message requiring processing.
     * @return success or failure status of the enqueueing operation.
     * @throws InterruptedException
     *         in case the current Thread was interrupted when waiting
     */
    boolean enqueueMessage(ChatMessage chatMessage) throws InterruptedException;

}
