package com.gmail.inverseconduit.commands;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * Simple handle for a Command. Consists of a {@link Predicate} to match
 * messages (aka. invocations) against, a helpText,
 * an infoText and a {@link Consumer Consumer} for {@link ChatMessage ChatMessages}
 * 
 * @author vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public class CommandHandle {

    private final Predicate<String>     matchesSyntax;

    private final String                helpText;

    private final String                infoText;

    private final Consumer<ChatMessage> executor;

    public CommandHandle(
        Predicate<String> matchesSyntax, Consumer<ChatMessage> executor,
        String helpText, String infoText) {
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
