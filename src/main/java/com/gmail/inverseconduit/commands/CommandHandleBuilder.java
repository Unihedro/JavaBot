package com.gmail.inverseconduit.commands;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * Command Builder for assembling commands. The command builder is not intended
 * to be threadsafe or reusable.
 * After building a command it should be disposed of, or undefined results may
 * occur
 * 
 * @author vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public final class CommandHandleBuilder {

    private static final Predicate<String> FALSE    = s -> false;
    
    private static final Logger            LOGGER   = Logger.getLogger(CommandHandleBuilder.class.getName());

    private Predicate<String>              matchesSyntax;

    private String                         helpText = "";

    private String                         infoText = "";

    private Consumer<ChatMessage>          executor;

    public CommandHandleBuilder() {
        matchesSyntax = FALSE;
    }

    /**
     * Build the command. Can be seen as ending the Builder's life span.
     * 
     * @return the resulting {@link CommandHandle} assembled from the actions
     *         performed on this instance.
     * @throws IllegalStateException
     *         if no syntax or execution were added.
     */
    public CommandHandle build() throws IllegalStateException {
        LOGGER.info("Building Command");
        if (null == executor) { throw new IllegalStateException("Internal builder state does not allow building command yet"); }

        return new CommandHandle(matchesSyntax, executor, helpText, infoText);
    }

    /**
     * Adds an additonal syntax to the command. Different Syntaxes are
     * shortcircuit-ORed together, as described by
     * {@link Predicate#or(Predicate) Predicate} This means if any of the given
     * syntaxes match the String later passed to the command it will return
     * true;
     * 
     * @param matcher
     *        A {@link Predicate Predicate<String>} describing the syntax
     * @return The Builder for chaining calls
     */
    public CommandHandleBuilder addSyntax(Predicate<String> matcher) {
        matchesSyntax = matchesSyntax.or(matcher);
        return this;
    }

    /**
     * Sets the command to be executed by the builder. Multiple calls to this
     * method overwrite each other, meaning only the latest given executor will
     * be in the built {@link CommandHandle}
     * 
     * @param executor
     *        the final version of the Command "handler"
     * @return The Builder for chaining calls
     */
    public CommandHandleBuilder setExecution(Consumer<ChatMessage> executor) {
        if (null != this.executor) {
            LOGGER.info("Overwriting existing executor");
        }

        this.executor = executor;
        return this;
    }

    /**
     * Sets the command's help text. The previously set helpText is overwritten,
     * only the latest given helpText will be added to the built {@link CommandHandle}
     * 
     * @param help
     *        The help text for the command
     * @return The Builder for chaining calls
     */
    public CommandHandleBuilder setHelpText(String help) {
        this.helpText = help;
        return this;
    }
    /**
    * Sets the command's info text. The previously set infoText is overwritten,
    * only the latest given infoText will be added to the built {@link CommandHandle}
    * 
    * @param info
    *        The info text for the command
    * @return The Builder for chaining calls
    */
    public CommandHandleBuilder setInfoText(String info) {
        this.infoText = info;
        return this;
    }
}
