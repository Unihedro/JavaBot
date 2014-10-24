package com.gmail.inverseconduit.commands;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.PrintUtils;
import com.gmail.inverseconduit.bot.AbstractBot;
import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;

import groovy.lang.GroovyCodeSource;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunScriptCommand implements ChatMessageListener {
	private final Logger logger = Logger.getLogger(ChatMessageListener.class.getName());
	
	private final Set<Integer> userIds = new HashSet<>();
	{
		userIds.add(3622940);
		userIds.add(2272617);
		userIds.add(1803692);
	}
	private final Set<Integer> blacklist = new HashSet<>();

	private final Pattern compileRegex = Pattern.compile("^"
			+ Pattern.quote(BotConfig.TRIGGER) + "java:(.*)");
	private final Pattern evalRegex = Pattern.compile("^"
			+ Pattern.quote(BotConfig.TRIGGER) + "eval:(.*)");
	private final Pattern lRegex = Pattern.compile("^"
			+ Pattern.quote(BotConfig.TRIGGER) + "load:(.*)");

	@Override
    public void onMessage(AbstractBot bot, ChatMessage msg) {
		//FIXME: Decouple the implementation from JavaBot class!
		JavaBot jBot = (JavaBot) bot;
        try {
            //System.out.println("Entered onMessage for RunScriptCommand");
            if (!userIds.contains(msg.getUserId())) return;
            logger.finest("Entered onMessage for RunScriptCommand");
            
            if (!userIds.contains(msg.getUserId()) || blacklist.contains(msg.getUserId()))  {
            	logger.finest("Ignoring message");
            	return;
            }
            String message = msg.getMessage();
            Matcher cMatcher = compileRegex.matcher(message);
            Matcher eMatcher = evalRegex.matcher(message);
            Matcher lMatcher = lRegex.matcher(message);

            if(cMatcher.find()) {
                compileAndExecuteMain(jBot, msg, cMatcher);
            } else if(lMatcher.find()) {
                compileAndCache(jBot, msg, cMatcher);
            } else if(eMatcher.find()) {
                evaluateGroovy(jBot, msg, eMatcher);
            }
        } catch(Exception ex) {
            jBot.sendMessage(msg.getSite(), msg.getRoomId(), PrintUtils.FixedFont(ex.getMessage()));
        }
    }

	private void evaluateGroovy(JavaBot bot, ChatMessage msg, Matcher eMatcher) {
		logger.finest("Evaluating Groovy Script");
		Object result = bot.getGroovyShell().evaluate(new GroovyCodeSource(eMatcher.group(1), "UserScript", "/sandboxScript"));
		bot.sendMessage(msg.getSite(), msg.getRoomId(), result.toString());
	}

	private void compileAndCache(JavaBot bot, ChatMessage msg, Matcher cMatcher) {
		logger.finest("Compiling class to cache it");
		Object gClass = bot.getGroovyLoader().parseClass(new GroovyCodeSource(cMatcher.group(1), "UserScript", "/sandboxScript"), true);
		bot.sendMessage(msg.getSite(), msg.getRoomId(), "Thanks, I'll remember that.");
	}

	private void compileAndExecuteMain(JavaBot bot, ChatMessage msg,
			Matcher cMatcher) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		logger.finest("Compiling class for execution");
		Object gClass = bot.getGroovyLoader().parseClass(new GroovyCodeSource(cMatcher.group(1), "UserScript", "/sandboxScript"), false);
		String result = ((Class) gClass).getMethod("main", String[].class).invoke(null, ((Object) new String[]{""})).toString();
		bot.sendMessage(msg.getSite(), msg.getRoomId(), result);
	}
}
