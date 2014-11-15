package com.gmail.inverseconduit.commands;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.gmail.inverseconduit.datatype.ChatMessage;


public class Command {
    private final Predicate<String> matchesSyntax;
    
    private final String helpText;
    private final String infoText;
    
    private final Consumer<ChatMessage> executor;
    
    public Command (Predicate<String> matchesSyntax, Consumer<ChatMessage> executor, String helpText, String infoText) {
        this.matchesSyntax = matchesSyntax;
        this.helpText = helpText;
        this.infoText = infoText;
        this.executor = executor;
    }
    
    public void execute(ChatMessage message) {
        executor.accept(message);
    }
    
    public boolean matchesSyntax(String commandCall) {
        return matchesSyntax.test(commandCall);
    }

    public String getHelpText() {
        return helpText;
    }

    public String getInfoText() {
        return infoText;
    }
}
