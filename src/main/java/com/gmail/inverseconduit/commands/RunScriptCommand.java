package com.gmail.inverseconduit.commands;

import groovy.lang.GroovyCodeSource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.bot.AbstractBot;
import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.chat.MessageListener;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.javadoc.ClassInfo;
import com.gmail.inverseconduit.javadoc.JavadocDao;
import com.gmail.inverseconduit.javadoc.JavadocZipDao;
import com.gmail.inverseconduit.javadoc.MultipleClassesFoundException;
import com.gmail.inverseconduit.utils.PrintUtils;

public class RunScriptCommand implements MessageListener {
	private static final Logger logger = Logger.getLogger(RunScriptCommand.class.getName());
	private static final JavadocDao javadocDao;
	static {
		if (Files.isDirectory(BotConfig.JAVADOCS_DIR)) {
			try {
				javadocDao = new JavadocZipDao(BotConfig.JAVADOCS_DIR);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			javadocDao = null;
		}
	}

	private final Set<Integer> userIds = new HashSet<>();
	{
		userIds.add(3622940);
		userIds.add(2272617);
		userIds.add(1803692);
		userIds.add(13379); //@Michael
	}
	private final Set<Integer> blacklist = new HashSet<>();

	private final Pattern messageRegex = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "(.*?):(.*)");

	@Override
    public void onMessage(AbstractBot bot, ChatMessage msg) {
		//FIXME: Decouple the implementation from JavaBot class!
		JavaBot jBot = (JavaBot) bot;
        try {
            logger.info("Entered onMessage for RunScriptCommand");
            
            if (!userIds.contains(msg.getUserId()) || blacklist.contains(msg.getUserId()))  {
            	logger.info("Ignoring message");
            	return;
            }
            
            String message = msg.getMessage();
            if (message.equals("test")){
                jBot.sendMessage(msg.getSite(), msg.getRoomId(), "*~response*");
            }
            
            Matcher messageMatcher = messageRegex.matcher(message);
            if (!messageMatcher.find()){
            	logger.info("Message is not a bot command.");
            	return;
            }
            
            String command = messageMatcher.group(1);
            String commandText = messageMatcher.group(2);
            switch(command){
            case "load":
            	compileAndCache(jBot, msg, commandText);
            	break;
            case "eval":
            	evaluateGroovy(jBot, msg, commandText);
            	break;
            case "java":
            	compileAndExecuteMain(jBot, msg, commandText);
            	break;
            case "javadoc":
            	javadoc(jBot, msg, commandText);
            	break;
            default:
            	jBot.sendMessage(msg.getSite(), msg.getRoomId(), "Sorry, I don't know that command. >.<");
            	break;
            }
        } catch(Exception ex) {
            jBot.sendMessage(msg.getSite(), msg.getRoomId(), PrintUtils.FixedFont(ex.getMessage()));
        }
    }

	private void evaluateGroovy(JavaBot bot, ChatMessage msg, String commandText) {
		logger.finest("Evaluating Groovy Script");
		Object result = bot.getGroovyShell().evaluate(new GroovyCodeSource(commandText, "UserScript", "/sandboxScript"));
		bot.sendMessage(msg.getSite(), msg.getRoomId(), result.toString());
	}

	private void compileAndCache(JavaBot bot, ChatMessage msg, String commandText) {
		logger.finest("Compiling class to cache it");
		Object gClass = bot.getGroovyLoader().parseClass(new GroovyCodeSource(commandText, "UserScript", "/sandboxScript"), true);
		bot.sendMessage(msg.getSite(), msg.getRoomId(), "Thanks, I'll remember that.");
	}

	private void compileAndExecuteMain(JavaBot bot, ChatMessage msg, String commandText) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		logger.finest("Compiling class for execution");
		Object gClass = bot.getGroovyLoader().parseClass(new GroovyCodeSource(commandText, "UserScript", "/sandboxScript"), false);
		String result = ((Class) gClass).getMethod("main", String[].class).invoke(null, ((Object) new String[]{""})).toString();
		bot.sendMessage(msg.getSite(), msg.getRoomId(), result);
	}

	private void javadoc(JavaBot bot, ChatMessage msg, String commandText) throws IOException {
		//TODO find classes that extend/implement the class
		//TODO show class hierarchy
		String message;
		if (javadocDao == null) {
			message = "Sorry, I can't answer that.  My Javadocs folder isn't configured!";
		} else {
			try {
				ClassInfo info = javadocDao.getClassInfo(commandText.trim());
				if (info == null) {
					message = "Sorry, I never heard of that class. :(";
				} else {
					message = info.getDescription().trim();
					int pos = message.indexOf("\n");
					if (pos >= 0) {
						//just display the first paragraph
						message = message.substring(0, pos);
					}
					
					StringBuilder sb = new StringBuilder();
					String fullName = info.getFullName();
					if (fullName.startsWith("java")){
						String url = "https://docs.oracle.com/javase/8/docs/api/index.html?" + fullName.replace('.',  '/') + ".html";
						sb.append("[**`").append(fullName).append("`**](").append(url).append(" \"view Javadocs\"): ");
					} else {
						sb.append("**`").append(fullName).append("`**: ");
					}
					
					sb.append(message);
					message = sb.toString();
				}
			} catch (MultipleClassesFoundException e) {
				StringBuilder sb = new StringBuilder("Which one do you mean?");
				for (String name : e.getClasses()) {
					sb.append("\n* ").append(name);
				}
				message = sb.toString();
			}
		}

		bot.sendMessage(msg.getSite(), msg.getRoomId(), message);
	}
}
