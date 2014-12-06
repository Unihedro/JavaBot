package com.gmail.inverseconduit.bot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.chat.ChatWorker;
import com.gmail.inverseconduit.chat.Subscribable;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;

/**
 * Defines bot core functionality. A bot manages {@link CommandHandle
 * CommandHandles}.
 * Additionally messages should be enqueued to him, using
 * {@link DefaultBot#enqueueMessage(ChatMessage) enqueueMessage}. <br />
 * <br />
 * These messages will get preprocessed and then passed to their respective
 * commandHandlers
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 * @author Vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public class DefaultBot implements Subscribable<CommandHandle>, ChatWorker {

    private final Logger                       LOGGER       =
                                                                    Logger.getLogger(DefaultBot.class.getName());

    protected final ScheduledExecutorService   executor     =
                                                                    Executors.newSingleThreadScheduledExecutor();

    protected final BlockingQueue<ChatMessage> messageQueue =
                                                                    new LinkedBlockingQueue<>();

    protected final Set<CommandHandle>         commands     = new HashSet<>();

    protected final ChatInterface              chatInterface;

    public DefaultBot(ChatInterface chatInterface) {
        this.chatInterface = chatInterface;
    }

    @Override
    public synchronized boolean enqueueMessage(ChatMessage chatMessage)
        throws InterruptedException {
        return messageQueue.offer(chatMessage, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void start() {
        executor.scheduleAtFixedRate(this::processMessageQueue, 1, 500, TimeUnit.MILLISECONDS);
    }

    private void processMessageQueue() {
        while (messageQueue.peek() != null) {
            LOGGER.info("processing message from queue");
            processMessage();
        }
    }

    private void processMessage() {
        final ChatMessage message = messageQueue.poll();
        commands.stream().filter(c -> c.matchesSyntax(message.getMessage())).findFirst().map(c -> {
            LOGGER.info("executing command:" + c.getName());
            return c.execute(message);
        }).ifPresent(msg -> chatInterface.sendMessage(SeChatDescriptor.buildSeChatDescriptorFrom(message), msg));
    }

    @Override
    public void subscribe(CommandHandle subscriber) {
        commands.add(subscriber);
    }

    @Override
    public void unSubscribe(CommandHandle subscriber) {
        commands.remove(subscriber);
    }

    public Set<CommandHandle> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    @Override
    protected void finalize() {
        executor.shutdownNow();
    }
}
