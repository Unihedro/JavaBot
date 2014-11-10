package com.gmail.inverseconduit;

import java.security.Policy;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.commands.RunScriptCommand;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;

public class Main {

    private static JavaBot javaBot;

    public static void main(String[] args) {
        Policy.setPolicy(ScriptSecurityPolicy.getInstance());
        System.setSecurityManager(ScriptSecurityManager.getInstance());

        javaBot = new JavaBot();
        boolean loggedIn = javaBot.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
        if ( !loggedIn) {
            Logger.getAnonymousLogger().severe("Login failed!");
            return;
        }

        javaBot.addListener(new RunScriptCommand());
        try {
            javaBot.joinChat(SESite.STACK_OVERFLOW, 139);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        executor.scheduleAtFixedRate(() -> {
            try {
                javaBot.processMessages();
            } catch(Exception e) {
                Logger.getAnonymousLogger().severe("Exception in processing thread: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
        Logger.getAnonymousLogger().info("Processing thread started");
        executor.scheduleAtFixedRate(() -> {
            try {
            javaBot.queryMessages(SESite.STACK_OVERFLOW, 139); //FIXME: refactor this..
            } catch (Exception e) {
                Logger.getAnonymousLogger().severe("Exception in querying thread: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
        Logger.getAnonymousLogger().info("querying thread started");
    }
}
