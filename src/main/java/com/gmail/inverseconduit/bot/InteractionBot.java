package com.gmail.inverseconduit.bot;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.gmail.inverseconduit.bot.interactions.Interaction;
import com.gmail.inverseconduit.bot.interactions.Interactions;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.chat.Subscribable;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;

public class InteractionBot extends AbstractBot implements Subscribable<Interaction> {

    private final ChatInterface      chatInterface;

    protected final Set<Interaction> interactions = new HashSet<>();

    public InteractionBot(ChatInterface chatInterface) {
        this.chatInterface = chatInterface;
        Interactions.getPerminteractions().forEach(interactions::add);
    }

    @Override
    public void start() {
        // asynchronously enqueue the processing by blocking supplier
        processingThread.submit(this::processInteractions);
    }

    private void processInteractions() {
        Stream.generate(blockingMessageSupplier).forEach(message -> processingThread.submit(() -> this.interact(message)));
    }

    private void interact(ChatMessage message) {
        interactions.stream().filter(interaction -> interaction.getCondition().test(message.getMessage())).findFirst()
                .ifPresent(action -> chatInterface.sendMessage(SeChatDescriptor.buildSeChatDescriptorFrom(message), action.getResponse()));
    }

    @Override
    public void subscribe(Interaction subscriber) {
        interactions.add(subscriber);
    }

    @Override
    public void unSubscribe(Interaction subscriber) {
        interactions.remove(subscriber);
    }

    @Override
    public Collection<Interaction> getSubscriptions() {
        return Collections.unmodifiableCollection(interactions);
    }

    @Override
    protected void shutdown() {
        // nothing to do here
    }
}
