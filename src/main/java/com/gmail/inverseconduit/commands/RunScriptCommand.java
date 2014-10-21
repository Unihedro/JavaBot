package com.gmail.inverseconduit.commands;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.JavaBot;
import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;
import groovy.lang.GroovyCodeSource;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunScriptCommand implements ChatMessageListener{
	private final Set<Integer> userIds = new HashSet<>();
	{
		userIds.add(3622940);
		userIds.add(2272617);
	}
	
	private final Pattern messageRegex = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "eval:(.*)");
	
    @Override
    public void onMessage(JavaBot bot, ChatMessage msg) {
        System.out.println("Entered onMessage for RunScriptCommand");
        if (!userIds.contains(msg.getUserId())){
        	//ignore message
        	return;
        }
        
        String message = msg.getMessage();
        Matcher matcher = messageRegex.matcher(message);
        if (!matcher.find()) {
        	//not a bot command
        	return;
        }

        String script = StringEscapeUtils.unescapeHtml4(matcher.group(1));
        System.out.println("Evaluating script: " + script);
        Object result = bot.getGroovyShell().evaluate(new GroovyCodeSource(script, "EvalCommand", "/sandboxScript"));
        System.out.println(result);
        bot.sendMessage(msg.getSite(), msg.getRoomId(), result.toString());
    }
}
