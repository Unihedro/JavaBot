package com.gmail.inverseconduit;

import com.gmail.inverseconduit.commands.RunScriptCommand;

public class Main {
    public static void main(String[] args) {
        try {
            JavaBot javaBot = new JavaBot();
            boolean loggedIn = javaBot.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
            if (!loggedIn) {
                System.out.println("Login failed!");
                return;
            }
            // Register command listeners
            javaBot.addListener(new RunScriptCommand());
            javaBot.joinChat(SESite.STACK_OVERFLOW, 1);
        } catch(IllegalStateException ex) {
            ex.printStackTrace();
        }
    }
}
