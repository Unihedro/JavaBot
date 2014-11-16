package com.gmail.inverseconduit.bot;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.gmail.inverseconduit.chat.Subscribable;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.datatype.ChatMessage;

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
public class DefaultBot implements Subscribable<CommandHandle> {

    private final Logger                       LOGGER       = Logger.getLogger(DefaultBot.class.getName());

    protected final BlockingQueue<ChatMessage> messageQueue = new LinkedBlockingQueue<>();

    protected final Set<CommandHandle>         commands     = new HashSet<>();

    public DefaultBot() {}

    public synchronized void processMessages() {
        LOGGER.finest("processing messages");

        while (true) {
            final ChatMessage message = messageQueue.poll();
            if (null == message) { return; }
            commands.stream()
                .filter(c -> c.matchesSyntax(message.getMessage()))
                .findFirst().ifPresent(c -> c.execute(message));
        }
    }

    public boolean enqueueMessage(ChatMessage chatMessage) throws InterruptedException {
        return messageQueue.offer(chatMessage, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void subscribe(CommandHandle subscriber) {
        commands.add(subscriber);
    }

    @Override
    public void unSubscribe(CommandHandle subscriber) {
        commands.remove(subscriber);
    }
}
