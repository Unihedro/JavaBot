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

    private final SESite        site;

    private final Path          javadocs;

    private final List<Integer> rooms;
    
    /**
     * List of userids with elevated privileges
     */
    private final List<Long> admins;

    /**
     * @param properties
     *        the properties file to pull the settings from
     */
    public BotConfig(Properties properties) {
        loginEmail = properties.getProperty("LOGIN-EMAIL");
        LOGGER.log(Level.CONFIG, "Setting loginEmail to " + loginEmail);

        password = properties.getProperty("PASSWORD");
        LOGGER.log(Level.CONFIG, "Setting password");

        trigger = properties.getProperty("TRIGGER", "!!");
        LOGGER.log(Level.CONFIG, "Setting trigger to " + trigger);

        String value = properties.getProperty("JAVADOCS", "javadocs");
        javadocs = Paths.get(value);
        LOGGER.log(Level.CONFIG, "Setting javadocs dir to " + javadocs);

        value = properties.getProperty("ROOMS", "1"); //default to "Sandbox"
        List<Integer> rooms = new ArrayList<>();
        for (String v : value.split("\\s*,\\s*")) { //split by comma
            try {
                Integer room = Integer.valueOf(v);
                rooms.add(room);
            } catch(NumberFormatException e) {
                LOGGER.log(Level.CONFIG, "Skipping unparsable room ID.");
                LOGGER.log(Level.FINEST, "", e);
            }
        }
        this.rooms = Collections.unmodifiableList(rooms);
        LOGGER.log(Level.CONFIG, "Setting rooms to " + rooms);
        
      //---------------------------------------
        value = properties.getProperty("ADMINS");
        List<Long> users = new ArrayList<>();
        for (String v : value.split("\\s*,\\s*")) {
        	try {
        		Long user = Long.valueOf(v);
        		users.add(user);
        	} catch (NumberFormatException e) {
        	    LOGGER.log(Level.CONFIG, "Skipping unparsable user ID.");
                LOGGER.log(Level.FINEST, "", e);
         	}
        }
        this.admins = users;
        //---------------------------------------

        this.site = SESite.fromUrl(properties.getProperty("SITE", "stackoverflow").toLowerCase());
        LOGGER.log(Level.CONFIG, "Setting site to " + site);
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
     * @return the string considered the trigger
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

    public List<Long> getAdmins() {
    	return admins;
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

    /**
     * Gets the Site configured as chathost and account host
     * 
     * @return the {@link SESite} considered as host. Defaults to
     *         {@link SESite#STACK_OVERFLOW STACK_OVERFLOW}
     */
    public SESite getSite() {
        return site;
    }
}
