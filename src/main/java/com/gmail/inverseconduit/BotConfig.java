package com.gmail.inverseconduit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.gmail.inverseconduit.datatype.CredentialsProvider;

/**
 * Holds environment settings, such as the bot's login credentials.
 */
public class BotConfig implements CredentialsProvider {

    private static final Logger LOGGER = Logger.getLogger(BotConfig.class.getName());

    private final String        loginEmail, password, trigger;

    private final Path          javadocs;

    private final List<Integer> rooms;

    /**
     * @param properties
     *        the properties file to pull the settings from
     */
    public BotConfig(Properties properties) {
        loginEmail = properties.getProperty("LOGIN-EMAIL");
        LOGGER.info("Setting loginEmail to " + loginEmail);

        password = properties.getProperty("PASSWORD");
        LOGGER.info("Setting password");

        trigger = properties.getProperty("TRIGGER", "!!");
        LOGGER.info("Setting trigger to " + trigger);

        String value = properties.getProperty("JAVADOCS", "javadocs");
        javadocs = Paths.get(value);
        LOGGER.info("Setting javadocs dir to " + javadocs);

        value = properties.getProperty("ROOMS", "1"); //default to "Sandbox"
        List<Integer> rooms = new ArrayList<>();
        for (String v : value.split("\\s*,\\s*")) { //split by comma
            try {
                Integer room = Integer.valueOf(v);
                rooms.add(room);
            } catch(NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Skipping unparsable room ID.", e);
            }
        }
        this.rooms = Collections.unmodifiableList(rooms);
        LOGGER.info("Setting rooms to " + rooms);
    }

    /**
     * Gets the login email address.
     * 
     * @return the login email address or null if not set
     */
    public String getLoginEmail() {
        return loginEmail;
    }

    /**
     * Gets the login password
     * 
     * @return the login password or null if not set
     */
    public String getLoginPassword() {
        return password;
    }

    /**
     * Gets the string sequence that triggers the bot.
     * 
     * @deprecated This constant is only here for legacy purposes. Use
     *             <tt>@ListenerProperty</tt> to specify the command invocation
     *             sequence(s).
     *             Will be removed when Subscribers system is finalized.
     */
    @Deprecated
    public String getTrigger() {
        return trigger;
    }

    /**
     * Gets the path to the Javadocs directory.
     * 
     * @return the path to the Javadocs directory (defaults to "javadocs")
     */
    public Path getJavadocsDir() {
        return javadocs;
    }

    /**
     * Gets the IDs of the rooms to join.
     * 
     * @return the room IDs (defaults to "1" for "Sandbox")
     */
    public List<Integer> getRooms() {
        return rooms;
    }

    @Override
    public String getIdentificator() {
        return loginEmail;
    }

    @Override
    public String getAuthenticator() {
        return password;
    }
}
