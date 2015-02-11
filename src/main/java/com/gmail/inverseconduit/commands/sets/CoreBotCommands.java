package com.gmail.inverseconduit.commands.sets;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.gmail.inverseconduit.AppContext;
import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.bot.Program;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.chat.Subscribable;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;
import com.gmail.inverseconduit.javadoc.JavaDocAccessor;
import com.gmail.inverseconduit.scripts.ScriptRunner;
import com.gmail.inverseconduit.timer.TimerCommands;

public final class CoreBotCommands {

    private static final BotConfig   BOT_CONFIG     = AppContext.INSTANCE.get(BotConfig.class);

    private static final Pattern     javadocPattern = Pattern.compile("^" + Pattern.quote(BOT_CONFIG.getTrigger()) + "javadoc (.*)", Pattern.DOTALL);

    private final JavaDocAccessor    javaDocAccessor;

    private final Set<CommandHandle> allCommands    = new HashSet<>();

    public CoreBotCommands(final ChatInterface chatInterface, final Subscribable<CommandHandle> commandOwner) {
        JavaDocAccessor tmp;
        //better not get ExceptionInInitializerError
        try {
            tmp = new JavaDocAccessor(BOT_CONFIG.getJavadocsDir());
        } catch(IOException ex) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Couldn't initialize Javadoc accessor.", ex);
            tmp = null;
        }
        this.javaDocAccessor = tmp;

        createSummonCommands(chatInterface);
        createShutdownCommand(chatInterface);
        createTimerCommand(chatInterface);

        createGroovyCommands();
        createJavaDocCommand();
        createNumberCommand();

        createHelpCommand(commandOwner);
        createListCommands(commandOwner);

        createAboutCommand();
        createTestCommand();
    }

    public Set<CommandHandle> allCommands() {
        return Collections.unmodifiableSet(allCommands);
    }

    private void createTimerCommand(ChatInterface chatInterface) {
        CommandHandle timers = TimerCommands.timerCommand(chatInterface);
        allCommands.add(timers);
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
        CommandHandle help =
                new CommandHandle.Builder("help", message -> {
                    String[] parts = message.getMessage().split(" ");
                    if (parts.length == 1) { // direct invocation
                        return "help command: Get additional info about a command of your choice, syntax:" + BOT_CONFIG.getTrigger() + "help [commandName]";
                    }
                    final String commandName = parts[parts.length - 1];
                    // should be only a single one (or none)
                    Stream<CommandHandle> possibleCommands = commandOwner.getSubscriptions().stream().filter(c -> c != null && commandName.equalsIgnoreCase(c.getName()));

                    Optional<String> helpText = possibleCommands.findFirst().map(c -> c.getHelpText());
                    if (helpText.isPresent()) { return helpText.get(); }
                    return "Could not find command with the name: " + commandName;
                }).setHelpText("help command: Get additional info about a command of your choice, syntax:" + BOT_CONFIG.getTrigger() + "help [commandName]")
                        .setInfoText("Get help for a specific command").build();
        allCommands.add(help);
    }

    private void createListCommands(final Subscribable<CommandHandle> commandOwner) {
        CommandHandle listCommand = new CommandHandle.Builder("listCommands", message -> {
            StringBuilder commandList = new StringBuilder("> Supported commands:\r\n");
            commandOwner.getSubscriptions().stream().map(handle -> String.format("- %s: %s\r\n", handle.getName(), handle.getInfoText())).forEach(commandList::append);
            return commandList.toString();
        }).setHelpText("listCommands: lists all available commands").setInfoText("show this command listing").build();
        allCommands.add(listCommand);
    }

    private void createJavaDocCommand() {
        CommandHandle javaDoc = new CommandHandle.Builder("javadoc", message -> {
            Matcher matcher = javadocPattern.matcher(message.getMessage());
            matcher.find();
            return javaDocAccessor.javadoc(message, matcher.group(1).trim());
        }).setInfoText("search javadocs for a specific Type or Method").build();
        allCommands.add(javaDoc);
    }

    private void createNumberCommand() {
        final Pattern p = Pattern.compile("^\\d+$");
        CommandHandle javaDoc = new CommandHandle.Builder(null, message -> {
            Matcher matcher = p.matcher(message.getMessage());
            if ( !matcher.find()) { return null; }

            int choice = Integer.parseInt(matcher.group(0));
            return javaDocAccessor.showChoice(message, choice);
        }).build();
        allCommands.add(javaDoc);
    }

    private void createShutdownCommand(ChatInterface chatInterface) {
        CommandHandle shutdown =
                new CommandHandle.Builder("shutdown", message -> {
                    chatInterface.broadcast("*~going down*");
                    chatInterface.getSubscriptions().forEach(s -> {
                        try {
                            s.enqueueMessage(Program.POISON_PILL);
                        } catch(Exception e) {
                            Logger.getAnonymousLogger().log(Level.WARNING, "Could not enqueue Poison Pill.", e);
                        }
                    });
                    Logger.getAnonymousLogger().log(Level.INFO, "Sent poison pill to all subscribers, shut down the querying thread");
                    return "";
                }).setInfoText("Shuts down the bot")
                        .setHelpText("This command requires owner privileges (or rather will do so). It shuts down the complete JavaBot instance in all chatRooms").build();
        allCommands.add(shutdown);
    }

    private void createTestCommand() {
        CommandHandle test = new CommandHandle.Builder("test", message -> {
            return "*~response*";
        }).build();
        allCommands.add(test);
    }
}
