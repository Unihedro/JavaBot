package com.gmail.inverseconduit.chat;

import com.gmail.inverseconduit.JavaBot;

public interface ChatMessageListener {
    public void onMessage(JavaBot bot, ChatMessage msg);
}
