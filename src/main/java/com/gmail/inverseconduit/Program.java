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
                chatInterface.queryMessages();
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
        bindSummonCommand();
        bindUnsummonCommand();
    }

    private void bindUnsummonCommand() {
        CommandHandle unsummon = new CommandHandle.Builder(s -> {
            return s.trim().equals(TRIGGER + "unsummon");
        }, message -> {
            chatInterface.sendMessage(message.getSite(), message.getRoomId(), "*~bye, bye*");
            chatInterface.leaveChat(message.getSite(), message.getRoomId());
        }).build();
        bot.subscribe(unsummon);
    }

    private void bindSummonCommand() {
        //FIXME: rewrite this to allow more actual wordings..
        CommandHandle summon = new CommandHandle.Builder(s -> {
            return s.trim().startsWith(TRIGGER + "summon") && s.trim().matches(".*summon (stack(overflow|exchange)|meta) [\\d]{1,6}");
        }, message -> {
            LOGGER.info("Summon command came in");
            String[] args = message.getMessage().trim().split(" ");
            final SESite targetSite;
            switch (args[1]) {
            case "stackoverflow":
                targetSite = SESite.STACK_OVERFLOW;
                break;
            case "stackexchange":
                targetSite = SESite.STACK_EXCHANGE;
                break;
            case "meta":
                targetSite = SESite.META_STACK_EXCHANGE;
                break;
            default:
                LOGGER.info("The given site was not one of stackoverflow, stackexchange or meta");
                chatInterface.sendMessage(message.getSite(), message.getRoomId(), "The given site was not one of stackoverflow, stackexchange or meta");
                return;
            }
            LOGGER.info("Determined SESite: " + targetSite);

            int targetRoom = Integer.parseInt(args[2]);
            chatInterface.joinChat(targetSite, targetRoom);
        }).build();
        bot.subscribe(summon);
    }

    private void bindHelpCommand() {
        CommandHandle help =
                new CommandHandle.Builder(
                    s -> {
                        return s.trim().startsWith(TRIGGER + "help");
                    },
                    message -> {
                        chatInterface.sendMessage(message.getSite(), message.getRoomId(), String.format("@%s I am JavaBot, maintained by Uni, Vogel, and a few others. You can find me on http://github.com/Vincentyification/JavaBot", message.getUsername()));
                    }).build();
        bot.subscribe(help);
    }

    private void bindShutdownCommand() {
        CommandHandle shutdown = new CommandHandle.Builder(s -> {
            return s.trim().startsWith(TRIGGER + "shutdown");
        }, message -> {
            //FIXME: Require permissions for this
            chatInterface.broadcast("*~going down*");
            executor.shutdownNow();
            System.exit(0);
        }).build();
        bot.subscribe(shutdown);
    }

    private void bindEvalCommand() {
        CommandHandle eval = new CommandHandle.Builder(evalPattern.asPredicate(), message -> {
            Matcher matcher = evalPattern.matcher(message.getMessage());
            matcher.find();
            scriptRunner.evaluateGroovy(message, matcher.group(1));
        }).setHelpText("Evaluates a given groovy script. Syntax: '{trigger}eval:{groovy}'").setInfoText("GroovyScript evalutation").build();
        bot.subscribe(eval);
    }

    private void bindJavaCommand() {
        CommandHandle java = new CommandHandle.Builder(javaPattern.asPredicate(), message -> {
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
        CommandHandle load = new CommandHandle.Builder(loadPattern.asPredicate(), message -> {
            Matcher matcher = loadPattern.matcher(message.getMessage());
            matcher.find();
            scriptRunner.evaluateAndCache(message, matcher.group(1));
        }).build();
        bot.subscribe(load);
    }

    private void bindJavaDocCommand() {
        CommandHandle javaDoc = new CommandHandle.Builder(javadocPattern.asPredicate(), message -> {
            Matcher matcher = javadocPattern.matcher(message.getMessage());
            matcher.find();
            javaDocAccessor.javadoc(message, matcher.group(1));
        }).build();
        bot.subscribe(javaDoc);
    }

    private void bindTestCommand() {
        CommandHandle test = new CommandHandle.Builder(s -> s.equals("test"), message -> {
            chatInterface.sendMessage(message.getSite(), message.getRoomId(), "*~response*");
        }).build();
        bot.subscribe(test);
    }

}
