package com.gmail.inverseconduit.chat;

import com.gmail.inverseconduit.bot.AbstractBot;
import com.gmail.inverseconduit.datatype.ChatMessage;

public interface MessageListener {

    public void onMessage(AbstractBot bot, ChatMessage msg);

}
