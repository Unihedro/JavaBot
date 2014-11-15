package com.gmail.inverseconduit;

import static com.gmail.inverseconduit.BotConfig.TRIGGER;

import java.security.Policy;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.chat.StackExchangeChat;
import com.gmail.inverseconduit.commands.Command;
import com.gmail.inverseconduit.commands.CommandBuilder;
import com.gmail.inverseconduit.commands.RunScriptCommand;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;

public class Main {

    // Prevents GC of loggers before htmlUnit can get them again...
    private static final Set<Logger> disabledLoggers = new HashSet<>();

    static {
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter"));
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.DefaultCssErrorHandler"));
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl"));
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.html.InputElementFactory"));
    }

    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
    
    public static void main(String[] args) {
        // HtmlUnit didn't properly clean up, so we have to
        disabledLoggers.forEach(l -> l.setLevel(Level.OFF));

        Policy.setPolicy(ScriptSecurityPolicy.getInstance());
        System.setSecurityManager(ScriptSecurityManager.getInstance());

        StackExchangeChat chatInterface = new StackExchangeChat();
        JavaBot javaBot = new JavaBot();

        chatInterface.subscribe(javaBot);
        javaBot.subscribe(new RunScriptCommand(chatInterface));

        Command help =
                new CommandBuilder().addSyntax(s-> {
                    return s.trim().startsWith(TRIGGER + "help");
                }).setExecution(message -> {
                    chatInterface.sendMessage(SESite.STACK_OVERFLOW, 139, String.format("@%s I am JavaBot, maintained by Uni, Vogel, and a few others. You can find me on http://github.com/Vincentyification/JavaBot", message.getUsername()));
                }).build();
        
        Command shutdown =
                new CommandBuilder().addSyntax(s-> {
                    return s.trim().startsWith(TRIGGER + "shutdown");
                }).setExecution(message -> {
                    chatInterface.sendMessage(SESite.STACK_OVERFLOW, 139, "*~going down*");
                    executor.shutdownNow();
                    System.exit(0);
                }).build();
        
        
        javaBot.commands.add(help);
        javaBot.commands.add(shutdown);

        boolean loggedIn = chatInterface.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
        if ( !loggedIn) {
            Logger.getAnonymousLogger().severe("Login failed!");
            System.exit( -255);
            return;
        }

        chatInterface.joinChat(SESite.STACK_OVERFLOW, 139);

        executor.scheduleAtFixedRate(() -> {
            try {
                javaBot.processMessages();
            } catch(Exception e) {
                Logger.getAnonymousLogger().severe("Exception in processing thread: " + e.getMessage());
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
        Logger.getAnonymousLogger().info("Processing thread started");
        executor.scheduleAtFixedRate(() -> {
            try {
                // FIXME: someone needs to keep track of the rooms we query!
                chatInterface.queryMessages(SESite.STACK_OVERFLOW, 139);
            } catch(Exception e) {
                Logger.getAnonymousLogger().severe("Exception in querying thread: " + e.getMessage());
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
        Logger.getAnonymousLogger().info("querying thread started");
    }
}
