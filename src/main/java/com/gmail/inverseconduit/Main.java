package com.gmail.inverseconduit;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Policy;
import java.util.Properties;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.gmail.inverseconduit.bot.Program;
import com.gmail.inverseconduit.chat.StackExchangeChat;
import com.gmail.inverseconduit.commands.sets.CoreBotCommands;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;

public class Main {

    public static void main(String[] args) throws Exception {
        setupLogging();

        //sandbox this ...
        Policy.setPolicy(ScriptSecurityPolicy.getInstance());
        System.setSecurityManager(ScriptSecurityManager.getInstance());

        BotConfig config = loadConfig();
        AppContext.INSTANCE.add(config);

        StackExchangeChat seInterface = new StackExchangeChat();
        Program p = new Program(seInterface);

        // Binds all core commands to the Bot (move to Bot instantiation??)
        new CoreBotCommands(seInterface, p.getBot()).allCommands().forEach(p.getBot()::subscribe);

        p.startup();
    }

    private static void setupLogging() {
        Filter filter = new Filter() {

            private final String packageName = Main.class.getPackage().getName();

            @Override
            public boolean isLoggable(LogRecord record) {
                //only log messages from this app
                String name = record.getLoggerName();
                return (name == null)
                    ? false
                    : name.startsWith(packageName);
            }
        };

        Logger global = Logger.getLogger("");
        for (Handler handler : global.getHandlers()) {
            handler.setFilter(filter);
        }
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
