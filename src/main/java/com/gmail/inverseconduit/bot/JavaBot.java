package com.gmail.inverseconduit.bot;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Logger;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.ScriptBase;
import com.gmail.inverseconduit.chat.MessageListener;
import com.gmail.inverseconduit.chat.StackExchangeChat;
import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * Procrastination: I'll fix this javadoc comment later.<br>
 * JavaBot @ com.gmail.inverseconduit
 *
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
public final class JavaBot extends AbstractBot {

    private static final Logger                 LOGGER        = Logger.getLogger(JavaBot.class.getName());

    private final StackExchangeChat             seChat;

    private final ArrayList<MessageListener>    listeners     = new ArrayList<>();

    private final Binding                       scriptBinding = new Binding();

    private final GroovyShell                   groovyShell;

    private final CompilerConfiguration         groovyConfig;

    private final GroovyClassLoader             groovyLoader;

    private final ExecutorService               threadPool    = Executors.newCachedThreadPool();

    private final SynchronousQueue<ChatMessage> messageQueue  = new SynchronousQueue<>(true);

    public JavaBot() {
        seChat = new StackExchangeChat(this);

        // Groovy
        groovyConfig = new CompilerConfiguration();
        groovyConfig.setScriptBaseClass(ScriptBase.class.getName());
        scriptBinding.setVariable("javaBot", this);
        groovyLoader = new GroovyClassLoader(JavaBot.class.getClassLoader(), groovyConfig);
        groovyShell = new GroovyShell(this.getClass().getClassLoader(), scriptBinding, groovyConfig);
    }

    public GroovyShell getGroovyShell() {
        return groovyShell;
    }

    public GroovyClassLoader getGroovyLoader() {
        return groovyLoader;
    }

    public boolean login(SESite site, String username, String password) {
        return seChat.login(site, username, password);
    }

    public boolean joinChat(SESite site, int chatId) {
        return seChat.joinChat(site, chatId);
    }

    public boolean sendMessage(SESite site, int chatId, String message) {
        return seChat.sendMessage(site, chatId, message);
    }

    @Override
    public void processMessages() {
        LOGGER.info("processing messages");
        try {
            final ChatMessage message = messageQueue.take();
            System.out.println(message.toString());
            threadPool.execute(() -> {
                for (MessageListener listener : getListeners())
                    listener.onMessage(this, message);
            });
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void queryMessages(SESite site, int chatId) {
        LOGGER.info("querying for new messages");
        seChat.queryMessages(site, chatId);
    }

    public void queueMessage(ChatMessage message) {
        messageQueue.add(message);
    }

}
