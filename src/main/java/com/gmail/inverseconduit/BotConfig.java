package com.gmail.inverseconduit;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class BotConfig {

    private static final Logger   LOGGER        = Logger.getLogger(BotConfig.class.getName());

    public static final BotConfig Configuration = new BotConfig();

    public final String           LOGIN_EMAIL;

    public final String           PASSWORD;

    public final Path             JAVADOCS_DIR;

    /**
     * @deprecated This constant is only here for legacy purposes. Use
     *             <tt>@ListenerProperty</tt> to specify the command invocation
     *             sequence(s). Will be removed when Subscribers system is
     *             finalized.
     */
    @Deprecated
    public final String           TRIGGER;

    String                        loginEmail    = "";

    String                        trigger       = "!!";

    String                        password      = "";

    String                        javadocs      = "javadocs";

    private BotConfig() {
        List<String> configSets;
        try {
            configSets = Files.readAllLines(Paths.get("bot.properties"), Charset.forName("UTF-8"));
        } catch(IOException e) {
            Logger.getAnonymousLogger().severe("Cannot read configuration file, falling back to default configuration");
            configSets = Collections.EMPTY_LIST;
            System.exit( -1); //Continuing execution is retarded...
        }

        configSets.stream().forEach(config -> {
            if (config.startsWith("LOGIN-EMAIL=")) {
                loginEmail = config.substring(config.indexOf("=") + 1);
                LOGGER.info("Setting loginEmail to " + loginEmail);
            }
            else if (config.startsWith("PASSWORD=")) {
                password = config.substring(config.indexOf("=") + 1);
                LOGGER.info("Setting password");
            }
            else if (config.startsWith("TRIGGER=")) {
                trigger = config.substring(config.indexOf("=") + 1);
                LOGGER.info("Setting trigger to " + trigger);
            }
            else if (config.startsWith("JAVADOCS=")) {
                javadocs = config.substring(config.indexOf("=") + 1);
                LOGGER.info("Setting javadocs dir to " + javadocs);
            }
        });

        LOGIN_EMAIL = loginEmail;
        PASSWORD = password;
        TRIGGER = trigger;
        JAVADOCS_DIR = Paths.get(javadocs);
    }
}
