package com.gmail.inverseconduit.bot;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.gmail.inverseconduit.chat.ChatWorker;
import com.gmail.inverseconduit.datatype.ChatMessage;

public abstract class AbstractBot implements ChatWorker {

    protected final ScheduledExecutorService   executor         = Executors.newSingleThreadScheduledExecutor();

    protected final ExecutorService            processingThread = Executors.newSingleThreadExecutor();

    protected final BlockingQueue<ChatMessage> messageQueue     = new LinkedBlockingQueue<>();

    @Override
    public final synchronized boolean enqueueMessage(ChatMessage chatMessage) throws InterruptedException {
        if (chatMessage == Program.POISON_PILL) {
            executor.shutdown();
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
     * request via {@link Program.POISON_PILL}
     */
    protected abstract void shutdown();
}
