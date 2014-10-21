package com.gmail.inverseconduit;

import com.gmail.inverseconduit.commands.RunScriptCommand;

public class Main {
    private static JavaBot javaBot;
    public static void main(String[] args) {
        javaBot = new JavaBot();
        javaBot.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
        // Register command listeners
        javaBot.addListener(new RunScriptCommand());

        javaBot.joinChat(SESite.STACK_OVERFLOW, 1);
    }
}
