package com.gmail.inverseconduit.commands;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.JavaBot;
import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;

public class RunScriptCommand implements ChatMessageListener{
    @Override
    public void onMessage(JavaBot bot, ChatMessage msg) {
        String m = msg.getMessage();
        if(msg.getMessage().startsWith(BotConfig.TRIGGER + " run")) {
            String script = m.substring(m.indexOf("`"), m.lastIndexOf("`"));
            //...
        }
    }
}
