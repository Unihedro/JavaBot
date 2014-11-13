package com.gmail.inverseconduit;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

public class BotConfig {

	private static final Logger LOGGER = Logger.getLogger(BotConfig.class.getName());
	private static final BotConfig INSTANCE = new BotConfig();

	private String loginEmail;
	private String password;
	private String trigger;

	/**
	 * @deprecated This constant is only here for legacy purposes. Use
	 *             <tt>@ListenerProperty</tt> to specify the command invocation
	 *             sequence(s). Will be removed when Subscribers system is
	 *             finalized.
	 */
	@Deprecated
	public static final String TRIGGER = "##";
	public static final Path JAVADOCS_DIR = Paths.get("javadocs");

	private BotConfig() {
		List<String> configSets;
		try {
			configSets = Files.readAllLines(Paths.get("bot.properties"), Charset.forName("UTF-8"));
		} catch (IOException e) {
			Logger.getAnonymousLogger().severe("Cannot read configuration file");
			e.printStackTrace();
			System.exit(-254);
			return;
		}
		
		configSets.stream().forEach(config -> {
			if (config.startsWith("LOGIN-EMAIL=")) {
				loginEmail = config.substring(config.indexOf("=") + 1);
				LOGGER.info("Setting loginEmail to " + loginEmail);
			} else if (config.startsWith("PASSWORD=")) {
				password = config.substring(config.indexOf("=") + 1);
				LOGGER.info("Setting password");
			} else if (config.startsWith("TRIGGER=")) {
				trigger = config.substring(config.indexOf("=") + 1);
				LOGGER.info("Setting trigger to " + trigger);
			}
		});
	}

	public static BotConfig getConfig() {
		return INSTANCE;
	}
	
	public String getLoginEmail() {
		return loginEmail;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getTrigger() {
		return trigger;
	}
}
