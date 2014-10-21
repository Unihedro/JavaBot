package com.gmail.inverseconduit.commands;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.JavaBot;
import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;

public class RunScriptCommand implements ChatMessageListener{
	private final Set<Integer> userIds = new HashSet<Integer>();
	{
		userIds.add(3622940);
		userIds.add(2272617);
	}
	
    @Override
    public void onMessage(JavaBot bot, ChatMessage msg) {
        System.out.println("Entered onMessage for RunScriptCommand");
        if (!userIds.contains(msg.getUserId())){
        	//ignore message
        	return;
        }
        
        String m = msg.getMessage();
        if (!msg.getMessage().startsWith(BotConfig.TRIGGER + "eval:")) {
        	//not a bot command
        	return;
        }

        String script = StringEscapeUtils.unescapeHtml4(m.substring(m.indexOf(":") + 1));
        System.out.println("Evaluating script: " + script);
        Object result = bot.getGroovyShell().evaluate(script);
        System.out.println(result);
        bot.sendMessage(msg.getSite(), msg.getRoomId(), result.toString());
    }
}
