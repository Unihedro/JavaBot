package com.gmail.inverseconduit.bot;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmail.inverseconduit.chat.ChatWorker;
import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * Abstract Class that implements default ChatWorker behavior.
 * Internally this class exposes the protected fields: {@link #messageQueue} and
 * {@link #processingThread}. </br>
 * This class implements thread-safe behavior to
 * enqueue messages to {@link #messageQueue}.
 * </br></br>If the {@link ChatWorker#POISON_PILL POISON_PILL} is enqueued,
 * {@link #shutdown()} will be called to notify implementing classes of the
 * shutdown request.
 * Also the {@link #processingThread}'s {@link ExecutorService#shutdown()
 * shutdown}-method will be called. It is accordingly not usable after this.
 * 
 * @author Vogel612<<a href="mailto:vogel612@gmx.de"
 *         >vogel612@gmx.de</a>>
 */
public abstract class AbstractBot implements ChatWorker {

    private static ThreadLocal<Boolean>        shutdown                = ThreadLocal.withInitial(() -> false);

    protected final ExecutorService            processingThread        = Executors.newFixedThreadPool(2);

    protected final BlockingQueue<ChatMessage> messageQueue            = new LinkedBlockingQueue<>();

    protected Supplier<ChatMessage>            blockingMessageSupplier = () -> {
                                                                           while ( !shutdown.get()) {
                                                                               ChatMessage headMessage = null;
                                                                               try {
                                                                                   headMessage = messageQueue.poll(10, TimeUnit.MINUTES);
                                                                               } catch(InterruptedException e) {
                                                                                   Logger.getLogger(this.getClass().getName()).log(Level.FINE,
                                                                                           "Didn't get a message over the course of 10 minutes... something might be wrong", e);
                                                                               }
                                                                               if (headMessage != null) {
                                                                                   Logger.getLogger(this.getClass().getName()).finest("processing message from queue");
                                                                                   return headMessage;
                                                                               }
                                                                           }
                                                                           // at this point, users of the supplier will have been shutdown, so it's moot
                                                                               return null;
                                                                           };

    @Override
    public final synchronized boolean enqueueMessage(ChatMessage chatMessage) throws InterruptedException {
        if (chatMessage == ChatWorker.POISON_PILL) {
            shutdown.set(true);
            processingThread.shutdown();
            this.shutdown();
            return true;
        }
        return messageQueue.offer(chatMessage, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public abstract void start();

    /**
     * Intended for shutting down any other Threads or executors declared in
     * extending classes.
     * This method will be called when the ChatWorker recieves a Shutdown
     * request via {@link ChatWorker#POISON_PILL}
     */
    protected abstract void shutdown();
}
