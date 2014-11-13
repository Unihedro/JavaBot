package com.gmail.inverseconduit;

import java.security.Policy;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.chat.StackExchangeChat;
import com.gmail.inverseconduit.commands.RunScriptCommand;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;

public class Main {

    public static void main(String[] args) {
        // HtmlUnit didn't properly clean up, so we have to
        Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter").setLevel(Level.OFF);
        Logger.getLogger("com.gargoylesoftware.htmlunit.DefaultCssErrorHandler").setLevel(Level.OFF);
        Logger.getLogger("com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl").setLevel(Level.OFF);
        Logger.getLogger("com.gargoylesoftware.htmlunit.html.InputElementFactory").setLevel(Level.OFF);

        Policy.setPolicy(ScriptSecurityPolicy.getInstance());
        System.setSecurityManager(ScriptSecurityManager.getInstance());

        StackExchangeChat chatInterface = new StackExchangeChat();
        JavaBot javaBot = new JavaBot();

        chatInterface.subscribe(javaBot);
        javaBot.subscribe(new RunScriptCommand(chatInterface));

        boolean loggedIn = chatInterface.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
        if ( !loggedIn) {
            Logger.getAnonymousLogger().severe("Login failed!");
            System.exit( -255);
            return;
        }

        chatInterface.joinChat(SESite.STACK_OVERFLOW, 139);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
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
