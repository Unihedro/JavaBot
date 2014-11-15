package com.gmail.inverseconduit.bot;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.jsoup.helper.Validate;

import com.gmail.inverseconduit.chat.ListenerProperty;
import com.gmail.inverseconduit.chat.ListenerProperty.Priority;
import com.gmail.inverseconduit.chat.MessageListener;
import com.gmail.inverseconduit.chat.Subscribable;
import com.gmail.inverseconduit.chat.TimedMessageListener;
import com.gmail.inverseconduit.commands.Command;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Abstact class to define bot core functionality. A bot can have subscribed
 * {@link MessageListener MessageListeners}.
 * Additionally messages can be enqueued to him, using
 * {@link AbstractBot#enqueueMessage(ChatMessage) enqueueMessage}. <br />
 * <br />
 * Implementations of this class are advised to use the already existing,
 * protected field {@link #messageQueue messageQueue} to handle messages from.
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 * @author Vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public abstract class AbstractBot implements Subscribable<MessageListener> {

    protected final BlockingQueue<ChatMessage>        messageQueue;

    /**
     * Maintains the collection of subscribers to the bot. A given Subscriber
     * may only exist <b>once</b> in the Map
     */
    private final Multimap<Priority, MessageListener> subscribers;

    protected AbstractBot() {
        messageQueue = new LinkedBlockingQueue<>();
        subscribers = Multimaps.newSetMultimap(new EnumMap<Priority, Collection<MessageListener>>(Priority.class), HashSet::new);
    }

    public abstract void processMessages();

    /**
     * @return An immutable list containing all subscribers
     */
    public List<MessageListener> getSubscribers() {
        return ImmutableList.copyOf(subscribers.values());
    }

    /**
     * Adds a subscriber to be notified of messages processed by this specific
     * Bot
     * 
     * @param listener
     *        The listener to be notified of messages
     */
    @Override
    public void subscribe(@Nonnull MessageListener listener) {
        Validate.notNull(listener);

        ListenerProperty property = listener.getClass().getAnnotation(ListenerProperty.class);
        if (null == property) {
            subscribers.put(Priority.DEFAULT, listener);
            return;
        }
        Priority priority = property.priority();

        int time = property.timer();
        if (time > 0) {
            listener = new TimedMessageListener(listener, this, time);
        }

        subscribers.put(priority, listener);
    }

    @Override
    public void unSubscribe(MessageListener listener) {
        if ( !subscribers.values().remove(listener)) {
            ListenerProperty property = listener.getClass().getAnnotation(ListenerProperty.class);
            if (null == property) { throw new IllegalStateException(); // maybe just return and swallow??
            }
            Priority priority = property.priority();
            int time = property.timer();
            if (time < 0) {
                listener = new TimedMessageListener(listener, this, time);
            }
            subscribers.remove(priority, listener);
        }
    }

    public boolean enqueueMessage(ChatMessage chatMessage) throws InterruptedException {
        return messageQueue.offer(chatMessage, 200, TimeUnit.MILLISECONDS);
    }
}
