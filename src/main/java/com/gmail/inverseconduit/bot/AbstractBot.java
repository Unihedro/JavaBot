package com.gmail.inverseconduit.bot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.jsoup.helper.Validate;

import com.gmail.inverseconduit.chat.ListenerProperty;
import com.gmail.inverseconduit.chat.MessageListener;
import com.gmail.inverseconduit.chat.TimedMessageListener;
import com.gmail.inverseconduit.chat.ListenerProperty.Priority;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public abstract class AbstractBot {

    protected final BlockingQueue<ChatMessage>        messageQueue;

    /**
     * @deprecated @see <a href="https://github.com/Vincentyification/JavaBot/issues/9"
     *             >#9</a>
     */
    @Deprecated
    private final Multimap<Priority, MessageListener> listeners;

    protected AbstractBot() {
        messageQueue = new LinkedBlockingQueue<>();
        listeners = Multimaps.newListMultimap(
                new EnumMap<Priority, Collection<MessageListener>>(Priority.class),
                ArrayList::new
            );
    }

    public abstract void processMessages();

    /**
     * @return An immutable list containing all message handlers.
     */
    public List<MessageListener> getListeners() {
        return ImmutableList.copyOf(listeners.values());
    }

    /**
     * Adds a listener to be notified of messages processed by this specific Bot
     * @param listener The listener to be notified of messages
     * @return a boolean indicating the succesfulness of the operation
     */
    public boolean addListener(@Nonnull MessageListener listener) {
        Validate.notNull(listener);

        ListenerProperty property = listener.getClass().getAnnotation(ListenerProperty.class);
        if (null == property)
            return listeners.put(Priority.DEFAULT, listener);
        Priority priority = property.priority();

        int time = property.timer();
        if (time > 0)
            listener = new TimedMessageListener(listener, this, time);

        return listeners.put(priority, listener);
    }

    public boolean removeListener(MessageListener listener) {
        return listeners.values().remove(listener);
    }

    public boolean enqueueMessage(ChatMessage chatMessage) throws InterruptedException {
        return messageQueue.offer(chatMessage, 200, TimeUnit.MILLISECONDS);
    }
}
