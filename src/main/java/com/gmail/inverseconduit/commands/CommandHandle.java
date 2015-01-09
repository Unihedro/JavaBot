package com.gmail.inverseconduit.commands;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * Simple handle for a Command. Consists of a {@link Predicate} to match
 * messages (aka. invocations) against, a helpText, an infoText and a
 * {@link Consumer Consumer} for {@link ChatMessage ChatMessages}
 * 
 * @author vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public class CommandHandle {

    private final String                        name;

    private final String                        helpText;

    private final String                        infoText;

    private final Function<ChatMessage, String> consumer;

    /**
     * Command Builder for assembling commands. The command builder is not
     * intended to be threadsafe or reusable. After building a command it should
     * be disposed of, or undefined results may occur
     * 
     * @author vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
     */
    public static class Builder {

        private String                        name;

        private String                        helpText = "";

        private String                        infoText = "";

        private Function<ChatMessage, String> consumer;

        @Deprecated
        @SuppressWarnings("unused")
        public Builder(String name, Predicate<String> matchesSyntax, Function<ChatMessage, String> consumer) {
            this.name = name;
            this.consumer = consumer;
        }

        public Builder(String name, Function<ChatMessage, String> consumer) {
            this.name = name;
            this.consumer = consumer;
        }

        /**
         * Build the command. Can be seen as ending the Builder's life span.
         * 
         * @return the resulting {@link CommandHandle} assembled from the
         *         actions performed on this instance.
         * @throws IllegalStateException
         *         if no syntax or execution were added.
         */
        public CommandHandle build() throws IllegalStateException {
            return new CommandHandle(this);
        }

        /**
         * Sets the command's help text. The previously set helpText is
         * overwritten, only the latest given helpText will be added to the
         * built {@link CommandHandle}
         * 
         * @param help
         *        The help text for the command
         * @return The Builder for chaining calls
         */
        public Builder setHelpText(String help) {
            this.helpText = help;
            return this;
        }

        /**
         * Sets the command's info text. The previously set infoText is
         * overwritten, only the latest given infoText will be added to the
         * built {@link CommandHandle}
         * 
         * @param info
         *        The info text for the command
         * @return The Builder for chaining calls
         */
        public Builder setInfoText(String info) {
            this.infoText = info;
            return this;
        }
    }

    private CommandHandle(Builder builder) {
        this.name = builder.name;
        this.helpText = builder.helpText;
        this.infoText = builder.infoText;
        this.consumer = builder.consumer;
    }

    public String execute(ChatMessage message) {
        return consumer.apply(message);
    }

    @Deprecated
    public boolean matchesSyntax(String commandCall) {
        // FIXME: Interimsimplementation. To be removed!
        return commandCall.contains(name);
    }

    public String getHelpText() {
        return helpText;
    }

    public String getInfoText() {
        return infoText;
    }

    public String getName() {
        return name;
    }
}
