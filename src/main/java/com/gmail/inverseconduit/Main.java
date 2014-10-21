package com.gmail.inverseconduit;

import com.gmail.inverseconduit.commands.RunScriptCommand;

public class Main {
    private static JavaBot javaBot;
    public static void main(String[] args) {
        try {
            javaBot = new JavaBot();
            javaBot.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
            javaBot.joinChat(SESite.STACK_OVERFLOW, 139);

            // Register command listeners
            javaBot.addListener(new RunScriptCommand());

        } catch(IllegalStateException ex) {
            ex.printStackTrace();
        }
    }
}
