package com.gmail.inverseconduit;

import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.commands.RunScriptCommand;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;

import java.security.Policy;

public class Main {
    private static JavaBot javaBot;
    public static void main(String[] args) {
        try {
            Policy.setPolicy(ScriptSecurityPolicy.getInstance());
            System.setSecurityManager(ScriptSecurityManager.getInstance());

            javaBot = new JavaBot();
            boolean loggedIn = javaBot.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
            if (!loggedIn) {
                System.out.println("Login failed!");
                return;
            }
            // Register command listeners
            javaBot.addListener(new RunScriptCommand());

            // Join chat
            javaBot.joinChat(SESite.STACK_OVERFLOW, 1);

            // Begin processing
            while(true) {
                javaBot.processMessages();
            }
        } catch(IllegalStateException ex) {
            ex.printStackTrace();
        }
    }
}
