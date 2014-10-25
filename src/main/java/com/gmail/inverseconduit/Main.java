package com.gmail.inverseconduit;

import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.commands.RunScriptCommand;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;

import java.security.Policy;

public class Main {
    public static void main(String[] args) {
        Policy.setPolicy(ScriptSecurityPolicy.getInstance());
        System.setSecurityManager(ScriptSecurityManager.getInstance());

        JavaBot javaBot = new JavaBot();
        boolean loggedIn = javaBot.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
        if (!loggedIn) {
            System.out.println("Login failed!");
            return;
        }
        
        javaBot.addListener(new RunScriptCommand());
        javaBot.joinChat(SESite.STACK_OVERFLOW, 1);

        while(true) {
            javaBot.processMessages();
        }
    }
}
