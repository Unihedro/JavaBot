package com.gmail.inverseconduit;

import static com.gmail.inverseconduit.BotConfig.TRIGGER;

import java.lang.reflect.InvocationTargetException;
import java.security.Policy;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.bot.DefaultBot;
import com.gmail.inverseconduit.chat.StackExchangeChat;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.commands.CommandHandleBuilder;
import com.gmail.inverseconduit.internal.ScriptRunner;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;

public class Main {

    // Prevents GC of loggers before htmlUnit can get them again...
    private static final Set<Logger> disabledLoggers = new HashSet<>();

    static {
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter"));
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.DefaultCssErrorHandler"));
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl"));
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.html.InputElementFactory"));
    }

    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
    private static ScriptRunner scriptRunner;
    
    private static final Pattern evalPattern = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "eval:(.*)", Pattern.DOTALL);
    private static final Pattern javaPattern = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "java:(.*)", Pattern.DOTALL);
    private static final Pattern loadPattern = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "load:(.*)", Pattern.DOTALL);
    private static final Pattern javadocPattern = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "javadoc:(.*)", Pattern.DOTALL);
    
    public static void main(String[] args) {
        // HtmlUnit didn't properly clean up, so we have to
        disabledLoggers.forEach(l -> l.setLevel(Level.OFF));

        Policy.setPolicy(ScriptSecurityPolicy.getInstance());
        System.setSecurityManager(ScriptSecurityManager.getInstance());

        StackExchangeChat chatInterface = new StackExchangeChat();
        DefaultBot bot = new DefaultBot();

        scriptRunner = new ScriptRunner(chatInterface);

        chatInterface.subscribe(bot);

        CommandHandle help =
                new CommandHandleBuilder().addSyntax(s-> {
                    return s.trim().startsWith(TRIGGER + "help");
                }).setExecution(message -> {
                    chatInterface.sendMessage(message.getSite(), message.getRoomId(), String.format("@%s I am JavaBot, maintained by Uni, Vogel, and a few others. You can find me on http://github.com/Vincentyification/JavaBot", message.getUsername()));
                }).build();
        
        CommandHandle shutdown =
                new CommandHandleBuilder().addSyntax(s-> {
                    return s.trim().startsWith(TRIGGER + "shutdown");
                }).setExecution(message -> {
                    //FIXME: broadcast!
                    chatInterface.sendMessage(message.getSite(), message.getRoomId(), "*~going down*");
                    executor.shutdownNow();
                    System.exit(0);
                }).build();
        
        CommandHandle eval =
                new CommandHandleBuilder().addSyntax(evalPattern.asPredicate()).setExecution(message -> {
                    Matcher matcher = evalPattern.matcher(message.getMessage());
                    matcher.find();
                    scriptRunner.evaluateGroovy(message, matcher.group(1));
                }).setHelpText("Evaluates a given groovy script. Syntax: '{trigger}eval:{groovy}'")
                .setInfoText("GroovyScript evalutation").build();
        
        CommandHandle java = 
                new CommandHandleBuilder().addSyntax(javaPattern.asPredicate()).setExecution(message -> {
                    Matcher matcher = javaPattern.matcher(message.getMessage());
                    matcher.find();
                    try {
                        scriptRunner.compileAndExecuteMain(message, matcher.group(1));
                    } catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        chatInterface.sendMessage(message.getSite(), message.getRoomId(), "No main-method found for execution");
                    }
                }).build();
              
        CommandHandle load =
                new CommandHandleBuilder().addSyntax(loadPattern.asPredicate()).setExecution(message -> {
                    Matcher matcher = loadPattern.matcher(message.getMessage());
                    matcher.find();
                    scriptRunner.evaluateAndCache(message, matcher.group(1));
                }).build();
        
        CommandHandle javaDoc = 
                new CommandHandleBuilder().addSyntax(javadocPattern.asPredicate()).setExecution(message -> {
                    Matcher matcher = javadocPattern.matcher(message.getMessage());
                    matcher.find();
                    scriptRunner.javadoc(message, matcher.group(1));
                }).build();
        
        CommandHandle test =
                new CommandHandleBuilder().addSyntax(s -> s.equals("test")).setExecution(message -> {
                    chatInterface.sendMessage(message.getSite(), message.getRoomId(), "*~response*");
                }).build();
        
        bot.subscribe(test);
        bot.subscribe(help);
        bot.subscribe(shutdown);
        bot.subscribe(eval);
        bot.subscribe(java);
        bot.subscribe(load);
        bot.subscribe(javaDoc);
        

        boolean loggedIn = chatInterface.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
        if ( !loggedIn) {
            Logger.getAnonymousLogger().severe("Login failed!");
            System.exit( -255);
            return;
        }

        chatInterface.joinChat(SESite.STACK_OVERFLOW, 139);

        executor.scheduleAtFixedRate(() -> {
            try {
                bot.processMessages();
            } catch(Exception e) {
                Logger.getAnonymousLogger().severe("Exception in processing thread: " + e.getMessage());
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
        Logger.getAnonymousLogger().info("Processing thread started");
        executor.scheduleAtFixedRate(() -> {
            try {
                // FIXME: someone needs to keep track of the rooms we query!
                chatInterface.queryMessages(SESite.STACK_OVERFLOW, 139);
            } catch(Exception e) {
                Logger.getAnonymousLogger().severe("Exception in querying thread: " + e.getMessage());
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
        Logger.getAnonymousLogger().info("querying thread started");
    }
}
