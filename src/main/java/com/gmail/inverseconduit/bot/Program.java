package com.gmail.inverseconduit.bot;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.AppContext;
import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.chat.ChatWorker;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;
import com.gmail.inverseconduit.javadoc.JavaDocAccessor;

/**
 * Class to contain the program, to be started from main. This class is
 * responsible for glueing all the components together.
 * 
 * @author vogel612<<a href="vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public class Program {

    private static final Logger     LOGGER         = Logger.getLogger(Program.class.getName());

    private static final BotConfig  config         = AppContext.INSTANCE.get(BotConfig.class);

    private final Set<ChatWorker>   bots           = new HashSet<>();

    private final ChatInterface     chatInterface;

    private final JavaDocAccessor   javaDocAccessor;

    private static final Pattern    javadocPattern = Pattern.compile("^" + Pattern.quote(config.getTrigger()) + "javadoc:(.*)", Pattern.DOTALL);

    public static final ChatMessage POISON_PILL    = new ChatMessage(null, -1, "", "", -1, "", -1);

    /**
     * @param chatInterface
     *        The ChatInterface to use as main interface to wire bots to. It is
     *        assumed that the ChatInterface's
     *        {@link ChatInterface#login(com.gmail.inverseconduit.datatype.ProviderDescriptor, com.gmail.inverseconduit.datatype.CredentialsProvider)
     *        login()} has been called already
     * @throws IOException
     *         if there's a problem loading the Javadocs
     */
    public Program(ChatInterface chatInterface) throws IOException {
        LOGGER.finest("Instantiating Program");
        this.chatInterface = chatInterface;
        bots.add(new DefaultBot(chatInterface));
        bots.add(new InteractionBot(chatInterface));

        JavaDocAccessor tmp;
        //better not get ExceptionInInitializerError
        try {
            tmp = new JavaDocAccessor(config.getJavadocsDir());
        } catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't initialize Javadoc accessor.", ex);
            tmp = null;
        }
        this.javaDocAccessor = tmp;

        bots.forEach(chatInterface::subscribe);
        LOGGER.info("Basic component setup complete");
    }

    /**
     * Here the injected chatInterface is used to join the rooms specified in
     * bot.properties.
     * Additionally all bots that were created on startup are started.
     */
    public void startup() {
        LOGGER.info("Beginning startup process");
        bindDefaultCommands();
        for (Integer room : config.getRooms()) {
            // FIXME: isn't always Stackoverflow
            chatInterface.joinChat(new SeChatDescriptor.DescriptorBuilder(SESite.STACK_OVERFLOW).setRoom(() -> room).build());
        }
        bots.forEach(ChatWorker::start);
        LOGGER.info("Startup completed.");
    }

    private void bindDefaultCommands() {
        bindNumberCommand();
        bindJavaDocCommand();
    }

    private void bindNumberCommand() {
        final Pattern p = Pattern.compile("^\\d+$");
        CommandHandle javaDoc = new CommandHandle.Builder(null, message -> {
            Matcher matcher = p.matcher(message.getMessage());
            if ( !matcher.find()) { return null; }

            int choice = Integer.parseInt(matcher.group(0));
            return javaDocAccessor.showChoice(message, choice);
        }).build();
        bot.subscribe(javaDoc);
    }

    private void bindJavaDocCommand() {
        CommandHandle javaDoc = new CommandHandle.Builder("javadoc", message -> {
            Matcher matcher = javadocPattern.matcher(message.getMessage());
            matcher.find();
            return javaDocAccessor.javadoc(message, matcher.group(1));
        }).build();
        bots.stream().filter(worker -> worker instanceof DefaultBot).findFirst().ifPresent(bot -> ((DefaultBot) bot).subscribe(javaDoc));
    }

    public Set<ChatWorker> getBots() {
        return bots;
    }
}
