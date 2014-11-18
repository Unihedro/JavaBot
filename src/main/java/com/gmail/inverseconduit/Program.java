package com.gmail.inverseconduit;

import static com.gmail.inverseconduit.BotConfig.TRIGGER;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.bot.DefaultBot;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.chat.StackExchangeChat;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.commands.CommandHandleBuilder;
import com.gmail.inverseconduit.internal.ScriptRunner;
import com.gmail.inverseconduit.javadoc.JavaDocAccessor;

/**
 * Class to contain the program, to be started from main. This class is
 * responsible for glueing all the components together.
 * 
 * @author vogel612<<a href="vogel612@gmx.de">vogel612@gmx.de</a>>
 */
@SuppressWarnings("deprecation")
public class Program {

    private static final Logger                      LOGGER         = Logger.getLogger(Program.class.getName());

    private static final ScheduledThreadPoolExecutor executor       = new ScheduledThreadPoolExecutor(2);

    private final DefaultBot                         bot;

    private final ChatInterface                      chatInterface;

    private final ScriptRunner                       scriptRunner;

    private final JavaDocAccessor                    javaDocAccessor;

    private static final Pattern                     evalPattern    = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "eval:(.*)", Pattern.DOTALL);

    private static final Pattern                     javaPattern    = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "java:(.*)", Pattern.DOTALL);

    private static final Pattern                     loadPattern    = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "load:(.*)", Pattern.DOTALL);

    private static final Pattern                     javadocPattern = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "javadoc:(.*)", Pattern.DOTALL);

    public Program() {
        LOGGER.finest("Instantiating Program");
        chatInterface = new StackExchangeChat();
        bot = new DefaultBot();

        chatInterface.subscribe(bot);

        javaDocAccessor = new JavaDocAccessor(chatInterface);
        scriptRunner = new ScriptRunner(chatInterface);
        LOGGER.info("Basic component setup complete");
    }

    /**
     * This is where the beef happens. Glue all the stuff together here
     */
    public void startup() {
        LOGGER.info("Beginning startup process");
        bindDefaultCommands();
        login();
        joinDefaultRoom();
        scheduleProcessingThread();
        scheduleQueryingThread();
        LOGGER.info("Startup completed.");
    }

    private void joinDefaultRoom() {
        chatInterface.joinChat(SESite.STACK_OVERFLOW, 139);
    }

    private void scheduleQueryingThread() {
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

    private void scheduleProcessingThread() {
        executor.scheduleAtFixedRate(() -> {
            try {
                bot.processMessages();
            } catch(Exception e) {
                Logger.getAnonymousLogger().severe("Exception in processing thread: " + e.getMessage());
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
        Logger.getAnonymousLogger().info("Processing thread started");
    }

    private void login() {
        boolean loggedIn = chatInterface.login(SESite.STACK_OVERFLOW, BotConfig.LOGIN_EMAIL, BotConfig.PASSWORD);
        if ( !loggedIn) {
            Logger.getAnonymousLogger().severe("Login failed!");
            System.exit( -255);
        }
    }

    private void bindDefaultCommands() {
        bindHelpCommand();
        bindShutdownCommand();
        bindEvalCommand();
        bindJavaCommand();
        bindLoadCommand();
        bindJavaDocCommand();
        bindTestCommand();
    }

    private void bindHelpCommand() {
        CommandHandle help =
                new CommandHandleBuilder().addSyntax(s -> {
                    return s.trim().startsWith(TRIGGER + "help");
                }).setExecution(message -> {
                    chatInterface.sendMessage(message.getSite(), message.getRoomId(), String.format("@%s I am JavaBot, maintained by Uni, Vogel, and a few others. You can find me on http://github.com/Vincentyification/JavaBot", message.getUsername()));
                }).build();
        bot.subscribe(help);
    }

    private void bindShutdownCommand() {
        CommandHandle shutdown = new CommandHandleBuilder().addSyntax(s -> {
            return s.trim().startsWith(TRIGGER + "shutdown");
        }).setExecution(message -> {
            //FIXME: broadcast!
            chatInterface.sendMessage(message.getSite(), message.getRoomId(), "*~going down*");
            executor.shutdownNow();
            System.exit(0);
        }).build();
        bot.subscribe(shutdown);
    }

    private void bindEvalCommand() {
        CommandHandle eval = new CommandHandleBuilder().addSyntax(evalPattern.asPredicate()).setExecution(message -> {
            Matcher matcher = evalPattern.matcher(message.getMessage());
            matcher.find();
            scriptRunner.evaluateGroovy(message, matcher.group(1));
        }).setHelpText("Evaluates a given groovy script. Syntax: '{trigger}eval:{groovy}'").setInfoText("GroovyScript evalutation").build();
        bot.subscribe(eval);
    }

    private void bindJavaCommand() {
        CommandHandle java = new CommandHandleBuilder().addSyntax(javaPattern.asPredicate()).setExecution(message -> {
            Matcher matcher = javaPattern.matcher(message.getMessage());
            matcher.find();
            try {
                scriptRunner.compileAndExecuteMain(message, matcher.group(1));
            } catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                chatInterface.sendMessage(message.getSite(), message.getRoomId(), "No main-method found for execution");
            }
        }).build();
        bot.subscribe(java);
    }

    private void bindLoadCommand() {
        CommandHandle load = new CommandHandleBuilder().addSyntax(loadPattern.asPredicate()).setExecution(message -> {
            Matcher matcher = loadPattern.matcher(message.getMessage());
            matcher.find();
            scriptRunner.evaluateAndCache(message, matcher.group(1));
        }).build();
        bot.subscribe(load);
    }

    private void bindJavaDocCommand() {
        CommandHandle javaDoc = new CommandHandleBuilder().addSyntax(javadocPattern.asPredicate()).setExecution(message -> {
            Matcher matcher = javadocPattern.matcher(message.getMessage());
            matcher.find();
            javaDocAccessor.javadoc(message, matcher.group(1));
        }).build();
        bot.subscribe(javaDoc);
    }

    private void bindTestCommand() {
        CommandHandle test = new CommandHandleBuilder().addSyntax(s -> s.equals("test")).setExecution(message -> {
            chatInterface.sendMessage(message.getSite(), message.getRoomId(), "*~response*");
        }).build();
        bot.subscribe(test);
    }

}
