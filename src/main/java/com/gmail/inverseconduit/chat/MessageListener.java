package com.gmail.inverseconduit.chat;

import com.gmail.inverseconduit.bot.AbstractBot;

public interface MessageListener {

    public void onMessage(AbstractBot bot, ChatMessage msg);

}
