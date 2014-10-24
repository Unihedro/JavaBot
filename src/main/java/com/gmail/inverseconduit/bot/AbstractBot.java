package com.gmail.inverseconduit.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;
import com.gmail.inverseconduit.chat.MessageRelay;

public abstract class AbstractBot {

	protected final BlockingQueue<ChatMessage> messageQueue = new LinkedBlockingQueue<>();
	protected final MessageRelay relay;
	protected final ArrayList<ChatMessageListener> listeners = new ArrayList<>();

	public AbstractBot() {
		relay = new MessageRelay(this);
	}

	public abstract void processMessages();

	public List<ChatMessageListener> getListeners() {
		return listeners;
	}

	public boolean addListener(ChatMessageListener listener) {
		return listeners.add(listener);
	}

	public boolean removeListener(ChatMessageListener listener) {
		return listeners.remove(listener);
	}

	public boolean enqueueMessage(ChatMessage chatMessage)
			throws InterruptedException {
		return messageQueue.offer(chatMessage, 200, TimeUnit.MILLISECONDS);
	}
}
