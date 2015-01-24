package com.gmail.inverseconduit.bot;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.AppContext;
import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.chat.ChatInterface;
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

    private final DefaultBot        bot;

    private final InteractionBot    interactionBot;

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
        this.bot = new DefaultBot(chatInterface);
        this.interactionBot = new InteractionBot(chatInterface);

        JavaDocAccessor tmp;
        //better not get ExceptionInInitializerError
        try {
            tmp = new JavaDocAccessor(config.getJavadocsDir());
        } catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't initialize Javadoc accessor.", ex);
            tmp = null;
        }
        this.javaDocAccessor = tmp;

        chatInterface.subscribe(bot);
        chatInterface.subscribe(interactionBot);
        LOGGER.info("Basic component setup complete");
    }

    /**
     * This is where the beef happens. Glue all the stuff together here
     */
    public void startup() {
        LOGGER.info("Beginning startup process");
        bindJavaDocCommand();
        for (Integer room : config.getRooms()) {
            // FIXME: isn't always Stackoverflow
            chatInterface.joinChat(new SeChatDescriptor.DescriptorBuilder(SESite.STACK_OVERFLOW).setRoom(() -> room).build());
        }
        bot.start();
        interactionBot.start();
        LOGGER.info("Startup completed.");
    }

    //FIXME: move this to the CoreBotCommands
    private void bindJavaDocCommand() {
        CommandHandle javaDoc = new CommandHandle.Builder("javadoc", message -> {
            Matcher matcher = javadocPattern.matcher(message.getMessage());
            matcher.find();
            return javaDocAccessor.javadoc(message, matcher.group(1).trim());
        }).build();
        bot.subscribe(javaDoc);
    }

    public DefaultBot getBot() {
        return bot;
    }
}
