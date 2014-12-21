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

	protected final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();

	protected final ExecutorService processingThread = Executors
			.newSingleThreadExecutor();

	protected final BlockingQueue<ChatMessage> messageQueue = new LinkedBlockingQueue<>();

	@Override
	public final synchronized boolean enqueueMessage(ChatMessage chatMessage)
			throws InterruptedException {
		return messageQueue.offer(chatMessage, 200, TimeUnit.MILLISECONDS);
	}

	@Override
	public abstract void start();

	@Override
	protected void finalize() {
		executor.shutdownNow();
	}

}
