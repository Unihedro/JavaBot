package com.gmail.inverseconduit.bot;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.AppContext;
import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.chat.StackExchangeChat;
import com.gmail.inverseconduit.chat.commands.ChatCommands;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;
import com.gmail.inverseconduit.javadoc.JavaDocAccessor;
import com.gmail.inverseconduit.scripts.ScriptRunner;
import com.gmail.inverseconduit.scripts.ScriptRunnerCommands;

/**
 * Class to contain the program, to be started from main. This class is
 * responsible for glueing all the components together.
 * 
 * @author vogel612<<a href="vogel612@gmx.de">vogel612@gmx.de</a>>
 */
@SuppressWarnings("deprecation")
public class Program {

    private static final Logger                   LOGGER         = Logger.getLogger(Program.class.getName());

    private static final ScheduledExecutorService executor       = Executors.newSingleThreadScheduledExecutor();

    private static final BotConfig                config         = AppContext.INSTANCE.get(BotConfig.class);

    private final DefaultBot                      bot;

    private final ChatInterface                   chatInterface;

    private final ScriptRunner                    scriptRunner;

    private final JavaDocAccessor                 javaDocAccessor;

    private static final Pattern                  javadocPattern = Pattern.compile("^" + Pattern.quote(config.getTrigger()) + "javadoc:(.*)", Pattern.DOTALL);

    /**
     * @throws IOException
     *         if there's a problem loading the Javadocs
     */
    public Program() throws IOException {
        LOGGER.finest("Instantiating Program");
        chatInterface = new StackExchangeChat();
        bot = new DefaultBot();

        chatInterface.subscribe(bot);

        javaDocAccessor = new JavaDocAccessor(chatInterface, config.getJavadocsDir());
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
        for (Integer room : config.getRooms()) {
            chatInterface.joinChat(SESite.STACK_OVERFLOW, room);
        }
        scheduleQueryingThread();
        bot.start();
        LOGGER.info("Startup completed.");
    }

    private void scheduleQueryingThread() {
        executor.scheduleAtFixedRate(() -> {
            try {
                chatInterface.queryMessages();
            } catch(Exception e) {
                Logger.getAnonymousLogger().severe("Exception in querying thread: " + e.getMessage());
                e.printStackTrace();
            }
        }, 5, 3, TimeUnit.SECONDS);
        Logger.getAnonymousLogger().info("querying thread started");
    }

    private void login() {
        boolean loggedIn = chatInterface.login(SESite.STACK_OVERFLOW, config);
        if ( !loggedIn) {
            Logger.getAnonymousLogger().severe("Login failed!");
            System.exit(2);
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
        CommandHandle unsummon = ChatCommands.unsummonCommand(chatInterface);
        bot.subscribe(unsummon);
    }

    private void bindSummonCommand() {
        CommandHandle summon = ChatCommands.summonCommand(chatInterface);
        bot.subscribe(summon);
    }

    private void bindEvalCommand() {
        CommandHandle eval = ScriptRunnerCommands.evalCommand(scriptRunner);
        bot.subscribe(eval);
    }

    private void bindJavaCommand() {
        CommandHandle java = ScriptRunnerCommands.javaCommand(scriptRunner, chatInterface);
        bot.subscribe(java);
    }

    private void bindLoadCommand() {
        CommandHandle load = ScriptRunnerCommands.loadCommand(scriptRunner);
        bot.subscribe(load);
    }

    private void bindHelpCommand() {
        CommandHandle help =
                new CommandHandle.Builder(
                    "help",
                    s -> {
                        return s.trim().startsWith(config.getTrigger() + "help");
                    },
                    message -> {
                        chatInterface.sendMessage(SeChatDescriptor.buildSeChatDescriptorFrom(message), String.format("@%s I am JavaBot, maintained by Uni, Vogel, and a few others. You can find me on http://github.com/Vincentyification/JavaBot", message.getUsername()));
                    }).build();
        bot.subscribe(help);
    }

    private void bindJavaDocCommand() {
        CommandHandle javaDoc = new CommandHandle.Builder("javadoc", javadocPattern.asPredicate(), message -> {
            Matcher matcher = javadocPattern.matcher(message.getMessage());
            matcher.find();
            javaDocAccessor.javadoc(message, matcher.group(1).trim());
        }).build();
        bot.subscribe(javaDoc);
    }

    private void bindShutdownCommand() {
        CommandHandle shutdown = new CommandHandle.Builder("shutdown", s -> {
            return s.trim().startsWith(config.getTrigger() + "shutdown");
        }, message -> {
            //FIXME: Require permissions for this
            chatInterface.broadcast("*~going down*");
            executor.shutdownNow();
            System.exit(0);
        }).build();
        bot.subscribe(shutdown);
    }

    private void bindTestCommand() {
        CommandHandle test = new CommandHandle.Builder("test", s -> s.equals("test"), message -> {
            chatInterface.sendMessage(SeChatDescriptor.buildSeChatDescriptorFrom(message), "*~response*");
        }).build();
        bot.subscribe(test);
    }
}
