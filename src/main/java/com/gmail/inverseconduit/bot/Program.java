package com.gmail.inverseconduit.bot;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.gmail.inverseconduit.AppContext;
import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.chat.ChatWorker;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;

/**
 * Class to contain the program, to be started from main. This class is
 * responsible for glueing all the components together.
 * 
 * @author vogel612<<a href="vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public class Program {

    private static final Logger     LOGGER      = Logger.getLogger(Program.class.getName());

    private static final BotConfig  config      = AppContext.INSTANCE.get(BotConfig.class);

    private final Set<ChatWorker>   bots        = new HashSet<>();

    private final ChatInterface     chatInterface;

    public static final ChatMessage POISON_PILL = new ChatMessage(null, -1, "", "", -1, "", -1);

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
        for (Integer room : config.getRooms()) {
            // FIXME: isn't always Stackoverflow
            chatInterface.joinChat(new SeChatDescriptor.DescriptorBuilder(SESite.STACK_OVERFLOW).setRoom(() -> room).build());
        }
        bots.forEach(ChatWorker::start);
        LOGGER.info("Startup completed.");
    }

    public Set<ChatWorker> getBots() {
        return bots;
    }
}
