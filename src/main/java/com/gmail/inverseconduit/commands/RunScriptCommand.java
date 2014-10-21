package com.gmail.inverseconduit.commands;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.JavaBot;
import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;
import org.apache.commons.lang3.StringEscapeUtils;

public class RunScriptCommand implements ChatMessageListener{
    @Override
    public void onMessage(JavaBot bot, ChatMessage msg) {
        System.out.println("Entered onMessage for RunScriptCommand");
        if(msg.getUserId() == 3622940 || msg.getUserId() == 2272617) {
            String m = msg.getMessage();
            if (msg.getMessage().startsWith(BotConfig.TRIGGER + "eval:")) {
                String script = StringEscapeUtils.unescapeHtml4(m.substring(m.indexOf(":") + 1));
                System.out.println("Evaluating script: " + script);
                Object result = bot.getGroovyShell().evaluate(script);
                System.out.println(result);
                bot.sendMessage(msg.getSite(), msg.getRoomId(), result.toString());
            }
        }
    }
}
