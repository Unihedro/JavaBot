package com.gmail.inverseconduit;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Policy;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.gmail.inverseconduit.bot.Program;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.chat.StackExchangeChat;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        setupLogging();

        //sandbox this ...
        Policy.setPolicy(ScriptSecurityPolicy.getInstance());
        System.setSecurityManager(ScriptSecurityManager.getInstance());

        BotConfig config = loadConfig();
        AppContext.INSTANCE.add(config);

        // Must be leaked from main thread
        StackExchangeChat seInterface = new StackExchangeChat();
        if ( !seInterface.login(SESite.STACK_OVERFLOW, config)) {
            LOGGER.severe("Login failed!");
            throw new RuntimeException("Login failure");
        }

        Program p = new Program(seInterface);

        p.startup();
        ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("message-query-thread-%d").build();
        Executors.newSingleThreadScheduledExecutor(factory).scheduleAtFixedRate(() -> queryMessagesFor(seInterface), 5, 3, TimeUnit.SECONDS);
    }

    private static void queryMessagesFor(ChatInterface seInterface) {
        try {
            seInterface.queryMessages();
        } catch(RuntimeException | Error e) {
            LOGGER.log(Level.SEVERE, "Runtime Exception or Error occurred:", e);
            throw e;
        } catch(Exception e) {
            LOGGER.log(Level.WARNING, "Exception occured:", e);
        }
    }

    private static void setupLogging() {
        Filter filter = new Filter() {

            private final String packageName = Main.class.getPackage().getName();

            @Override
            public boolean isLoggable(LogRecord record) {
                //only log messages from this app
                String name = record.getLoggerName();
                return name != null && name.startsWith(packageName);
            }
        };

        Logger global = Logger.getLogger("");
        Arrays.stream(global.getHandlers()).forEach(h -> h.setFilter(filter));
    }

    private static BotConfig loadConfig() throws IOException {
        Path file = Paths.get("bot.properties");
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
            properties.load(reader);
        }
        return new BotConfig(properties);
    }
}
