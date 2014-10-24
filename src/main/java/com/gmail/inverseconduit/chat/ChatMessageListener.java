package com.gmail.inverseconduit.chat;

import com.gmail.inverseconduit.bot.AbstractBot;;

public interface ChatMessageListener {
    public void onMessage(AbstractBot bot, ChatMessage msg);
}
