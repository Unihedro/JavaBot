package com.gmail.inverseconduit.scripts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.AppContext;
import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.commands.CommandHandle;

public final class ScriptRunnerCommands {

    private static final BotConfig config      = AppContext.INSTANCE.get(BotConfig.class);

    private static final Pattern   evalPattern = Pattern.compile("^" + Pattern.quote(config.getTrigger()) + "eval:(.*)", Pattern.DOTALL);

    private static final Pattern   loadPattern = Pattern.compile("^" + Pattern.quote(config.getTrigger()) + "load:(.*)", Pattern.DOTALL);

    public static CommandHandle evalCommand(ScriptRunner scriptRunner) {
        return new CommandHandle.Builder("eval", evalPattern.asPredicate(), message -> {
            Matcher matcher = evalPattern.matcher(message.getMessage());
            matcher.find();
            return scriptRunner.evaluateGroovy(message, matcher.group(1).trim());
        }).setHelpText("Evaluates a given groovy script. Syntax: '" + config.getTrigger() + "eval:{groovy}'").setInfoText("GroovyScript evaluation").build();
    }

    public static CommandHandle loadCommand(ScriptRunner scriptRunner) {
        return new CommandHandle.Builder("load", loadPattern.asPredicate(), message -> {
            Matcher matcher = loadPattern.matcher(message.getMessage());
            matcher.find();
            scriptRunner.evaluateAndCache(message, matcher.group(1));
            return "Thanks, I'll remember that";
        }).build();
    }
}
