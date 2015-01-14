package com.gmail.inverseconduit.commands.sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.gmail.inverseconduit.AppContext;
import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.chat.Subscribable;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.scripts.ScriptRunner;

public final class CoreBotCommands {

    private static final BotConfig BOT_CONFIG  = AppContext.INSTANCE.get(BotConfig.class);

    final Set<CommandHandle>       allCommands = new HashSet<>();

    public CoreBotCommands(final ChatInterface chatInterface, final Subscribable<CommandHandle> commandOwner) {
        createSummonCommands(chatInterface);
        createGroovyCommands();
        createHelpCommand(commandOwner);
        createListCommands(commandOwner);
        createAboutCommand();
        createTestCommand();
    }

    public Set<CommandHandle> allCommands() {
        return Collections.unmodifiableSet(allCommands);
    }

    private void createSummonCommands(ChatInterface chatInterface) {
        allCommands.add(ChatCommands.unsummonCommand(chatInterface));
        allCommands.add(ChatCommands.summonCommand(chatInterface));
    }

    private void createGroovyCommands() {
        ScriptRunner runner = new ScriptRunner();
        allCommands.add(ScriptRunnerCommands.evalCommand(runner));
        allCommands.add(ScriptRunnerCommands.loadCommand(runner));
    }

    private void createAboutCommand() {
        CommandHandle about =
                new CommandHandle.Builder("about", message -> {
                    return String.format("@%s I am JavaBot, maintained by Uni, Vogel, and a few others. You can find me on http://github.com/Vincentyification/JavaBot",
                            message.getUsername());
                }).build();
        allCommands.add(about);
    }

    private void createHelpCommand(final Subscribable<CommandHandle> commandOwner) {
        CommandHandle help = new CommandHandle.Builder("help", message -> {
            String[] parts = message.getMessage().split(" ");
            String commandName = parts[parts.length - 1];

            Optional<String> helpText = commandOwner.getSubscriptions().stream().filter(c -> c.getName().equals(commandName)).findFirst().map(c -> c.getHelpText());
            if (helpText.isPresent()) { return helpText.get(); }
            return "help command: Get additional info about a command of your choice, syntax:" + BOT_CONFIG.getTrigger() + "help [commandName]";
        }).setHelpText("help command: Get additional info about a command of your choice, syntax:" + BOT_CONFIG.getTrigger() + "help [commandName]").build();
        allCommands.add(help);
    }

    private void createListCommands(final Subscribable<CommandHandle> commandOwner) {
        CommandHandle listCommand = new CommandHandle.Builder("listCommands", message -> {
            StringBuilder commandList = new StringBuilder("> Supported commands:\r\n");
            commandOwner.getSubscriptions().stream().map(handle -> String.format("- %s: %s\r\n", handle.getName(), handle.getInfoText())).forEach(commandList::append);
            return commandList.toString();
        }).setHelpText("listCommands: lists all available commands").build();
        allCommands.add(listCommand);
    }

    // FIXME: Javadoc accessor needs configuration
    /*
     * private void bindJavaDocCommand() { CommandHandle javaDoc = new
     * CommandHandle.Builder("javadoc", message -> { Matcher matcher =
     * javadocPattern.matcher(message .getMessage()); matcher.find(); return
     * javaDocAccessor.javadoc(message, matcher.group(1) .trim()); }).build();
     * allCommands.add(javaDoc); }
     */

    /*
     * private void bindShutdownCommand() { CommandHandle shutdown = new
     * CommandHandle.Builder("shutdown", message -> { // FIXME: Require
     * permissions for this chatInterface.broadcast("*~going down*"); // FIXME:
     * needs to be solved differently executor.shutdownNow(); System.exit(0);
     * return ""; }).build(); allCommands.add(shutdown); }
     */

    private void createTestCommand() {
        CommandHandle test = new CommandHandle.Builder("test", message -> {
            return "*~response*";
        }).build();
        allCommands.add(test);
    }
}
