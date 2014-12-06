package com.gmail.inverseconduit.bot;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
public class Program {

	private static final Logger LOGGER = Logger.getLogger(Program.class
			.getName());

	private static final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();

	private static final BotConfig config = AppContext.INSTANCE
			.get(BotConfig.class);

	private final DefaultBot bot;

	private final ChatInterface chatInterface = new StackExchangeChat();

	private final ScriptRunner scriptRunner = new ScriptRunner();

	private final JavaDocAccessor javaDocAccessor;

	private static final Pattern javadocPattern = Pattern.compile(
			"^" + Pattern.quote(config.getTrigger()) + "javadoc:(.*)",
			Pattern.DOTALL);

	/**
	 * @throws IOException
	 *             if there's a problem loading the Javadocs
	 */
	public Program() throws IOException {
		LOGGER.finest("Instantiating Program");
		bot = new DefaultBot(chatInterface);

		chatInterface.subscribe(bot);

		javaDocAccessor = new JavaDocAccessor(config.getJavadocsDir());
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
			chatInterface.joinChat(new SeChatDescriptor.DescriptorBuilder(
					SESite.STACK_OVERFLOW).setRoom(() -> room).build());
		}
		scheduleQueryingThread();
		bot.start();
		LOGGER.info("Startup completed.");
	}

	private void scheduleQueryingThread() {
		executor.scheduleAtFixedRate(
				() -> {
					try {
						chatInterface.queryMessages();
					} catch (RuntimeException | Error e) {
						Logger.getAnonymousLogger()
								.log(Level.SEVERE,
										"Runtime Exception or Error occurred in querying thread",
										e);
						throw e;
					} catch (Exception e) {
						Logger.getAnonymousLogger().log(Level.WARNING,
								"Exception occured in querying thread:", e);
					}
				}, 5, 3, TimeUnit.SECONDS);
		Logger.getAnonymousLogger().info("querying thread started");
	}

	private void login() {
		boolean loggedIn = chatInterface.login(SESite.STACK_OVERFLOW, config);
		if (!loggedIn) {
			Logger.getAnonymousLogger().severe("Login failed!");
			System.exit(2);
		}
	}

	private void bindDefaultCommands() {
		bindAboutCommand();
		bindHelpCommand();
		bindShutdownCommand();
		bindEvalCommand();
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

	private void bindLoadCommand() {
		CommandHandle load = ScriptRunnerCommands.loadCommand(scriptRunner);
		bot.subscribe(load);
	}

	private void bindHelpCommand() {
		CommandHandle help = new CommandHandle.Builder("help", s -> s.trim()
				.matches(Pattern.quote(config.getTrigger()) + "help [^ ]++"),
				message -> {
					String[] parts = message.getMessage().split(" ");
					String commandName = parts[parts.length - 1];
					return bot.getCommands().stream()
							.filter(c -> c.getName().equals(commandName))
							.findFirst().map(c -> c.getHelpText()).get();
				}).setHelpText(
				"help command: Get additional info about a command of your choice, syntax:"
						+ config.getTrigger() + "help [commandName]").build();
		bot.subscribe(help);
	}

	private void bindAboutCommand() {
		CommandHandle about = new CommandHandle.Builder(
				"about",
				s -> {
					return s.trim().startsWith(config.getTrigger() + "about");
				},
				message -> {
					return String
							.format("@%s I am JavaBot, maintained by Uni, Vogel, and a few others. You can find me on http://github.com/Vincentyification/JavaBot",
									message.getUsername());
				}).build();
		bot.subscribe(about);
	}

	private void bindJavaDocCommand() {
		CommandHandle javaDoc = new CommandHandle.Builder("javadoc",
				javadocPattern.asPredicate(), message -> {
					Matcher matcher = javadocPattern.matcher(message
							.getMessage());
					matcher.find();
					javaDocAccessor.javadoc(message, matcher.group(1).trim());
					return javaDocAccessor.javadoc(message, matcher.group(1)
							.trim());
				}).build();
		bot.subscribe(javaDoc);
	}

	private void bindShutdownCommand() {
		CommandHandle shutdown = new CommandHandle.Builder("shutdown", s -> {
			return s.trim().startsWith(config.getTrigger() + "shutdown");
		}, message -> {
			// FIXME: Require permissions for this
				chatInterface.broadcast("*~going down*");
				executor.shutdownNow();
				System.exit(0);
				return "";
			}).build();
		bot.subscribe(shutdown);
	}

	private void bindTestCommand() {
		CommandHandle test = new CommandHandle.Builder("test",
				s -> s.equals("test"), message -> {
					return "*~response*";
				}).build();
		bot.subscribe(test);
	}
}
