package com.gmail.inverseconduit;

public class Main {
    public static void main(String[] args) {
        try {
            JavaBot javaBot = new JavaBot();
            javaBot.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
            javaBot.joinChat(SESite.STACK_OVERFLOW, 139);
        } catch(IllegalStateException ex) {
            ex.printStackTrace();
        }
    }
}
