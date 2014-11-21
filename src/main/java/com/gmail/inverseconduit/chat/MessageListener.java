package com.gmail.inverseconduit.chat;

import com.gmail.inverseconduit.bot.DefaultBot;
import com.gmail.inverseconduit.datatype.ChatMessage;

@Deprecated
public interface MessageListener {

    public void onMessage(DefaultBot bot, ChatMessage msg);

}
