package com.gmail.inverseconduit.commands;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.JavaBot;
import com.gmail.inverseconduit.PrintUtils;
import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;
import groovy.lang.GroovyCodeSource;

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
	
	private final Pattern compileRegex = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "java:(.*)");
    private final Pattern evalRegex = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "eval:(.*)");
    private final Pattern lRegex = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "load:(.*)");

    @Override
    public synchronized void onMessage(JavaBot bot, ChatMessage msg) {
        try {
            //System.out.println("Entered onMessage for RunScriptCommand");
            if (!userIds.contains(msg.getUserId())) return;

            String message = msg.getMessage();
            Matcher cMatcher = compileRegex.matcher(message);
            Matcher eMatcher = evalRegex.matcher(message);
            Matcher lMatcher = lRegex.matcher(message);

            // Compile class and call main method
            if(cMatcher.find()) {
                Object gClass = bot.getGroovyLoader().parseClass(new GroovyCodeSource(cMatcher.group(1), "UserScript", "/sandboxScript"), false);
                Object result = ((Class) gClass).getMethod("run", Object[].class).invoke(null, ((Object) new String[]{""}));
                if(result != null) bot.sendMessage(msg.getSite(), msg.getRoomId(), result.toString());
                else bot.sendMessage(msg.getSite(), msg.getRoomId(), "*Execution completed, null or no output*");
            }

            // Compile class, cache for later use.
            else if(lMatcher.find()) {
                Object gClass = bot.getGroovyLoader().parseClass(new GroovyCodeSource(cMatcher.group(1), "UserScript", "/sandboxScript"), true);
                //String result = ((Class) gClass).getMethod("main", String[].class).invoke(null, ((Object) new String[]{""})).toString();
                bot.sendMessage(msg.getSite(), msg.getRoomId(), "Thanks, I'll remember that.");
            }

            // Evaluate groovy in shell
            else if(eMatcher.find()) {
                Object result = bot.getGroovyShell().evaluate(new GroovyCodeSource(eMatcher.group(1), "UserScript", "/sandboxScript"));
                if(result != null) bot.sendMessage(msg.getSite(), msg.getRoomId(), result.toString());
                else bot.sendMessage(msg.getSite(), msg.getRoomId(), "*Execution completed, null or no output*");
            }
        } catch(Exception ex) {
            bot.sendMessage(msg.getSite(), msg.getRoomId(), PrintUtils.FixedFont(ex.getMessage()));
        }
    }
}
