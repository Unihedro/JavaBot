package com.gmail.inverseconduit.scripts;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.AppContext;
import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;

public final class ScriptRunnerCommands {
	private static final BotConfig config = AppContext.INSTANCE.get(BotConfig.class);
	
    private static final Pattern evalPattern = Pattern.compile("^" + Pattern.quote(config.getTrigger()) + "eval:(.*)", Pattern.DOTALL);

    private static final Pattern javaPattern = Pattern.compile("^" + Pattern.quote(config.getTrigger()) + "java:(.*)", Pattern.DOTALL);

    private static final Pattern loadPattern = Pattern.compile("^" + Pattern.quote(config.getTrigger()) + "load:(.*)", Pattern.DOTALL);

    public static CommandHandle evalCommand(ScriptRunner scriptRunner) {
        return new CommandHandle.Builder("eval", evalPattern.asPredicate(), message -> {
            Matcher matcher = evalPattern.matcher(message.getMessage());
            matcher.find();
            scriptRunner.evaluateGroovy(message, matcher.group(1));
        }).setHelpText("Evaluates a given groovy script. Syntax: '{trigger}eval:{groovy}'").setInfoText("GroovyScript evalutation").build();
    }

    public static CommandHandle javaCommand(ScriptRunner scriptRunner, ChatInterface chatInterface) {
        return new CommandHandle.Builder("java", javaPattern.asPredicate(), message -> {
            Matcher matcher = javaPattern.matcher(message.getMessage());
            matcher.find();
            try {
                scriptRunner.compileAndExecuteMain(message, matcher.group(1));
            } catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                chatInterface.sendMessage(SeChatDescriptor.buildSeChatDescriptorFrom(message), "No main-method found for execution");
            }
        }).build();
    }

    public static CommandHandle loadCommand(ScriptRunner scriptRunner) {
        return new CommandHandle.Builder("load", loadPattern.asPredicate(), message -> {
            Matcher matcher = loadPattern.matcher(message.getMessage());
            matcher.find();
            scriptRunner.evaluateAndCache(message, matcher.group(1));
        }).build();
    }
}
