package com.gmail.inverseconduit.chat;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.gmail.inverseconduit.bot.DefaultBot;
import com.gmail.inverseconduit.datatype.ChatMessage;

@Deprecated
public final class TimedMessageListener implements MessageListener {

    private MessageListener listener;

    private boolean         expired;

    public TimedMessageListener(@Nonnull MessageListener listener, DefaultBot bot, int time) { // Change to caller sensitivity
        this.listener = listener;
        Executors.newSingleThreadScheduledExecutor().schedule(new ExpiraryTask(bot, this), time, TimeUnit.SECONDS);
    }

    private static final class ExpiraryTask implements Runnable {

        private final DefaultBot          bot;

        private final TimedMessageListener listener;

        ExpiraryTask(DefaultBot bot, TimedMessageListener listener) {
            this.bot = bot;
            this.listener = listener;
        }

        @Override
        public void run() {
            //"unsubscribe" from bot"
            listener.expired = true;
            listener.listener = null;
        }

    }

    @Override
    public void onMessage(DefaultBot bot, ChatMessage msg) {
        if ( !expired)
            listener.onMessage(bot, msg);
    }

}