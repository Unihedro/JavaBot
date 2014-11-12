package com.gmail.inverseconduit.bot;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.gmail.inverseconduit.ScriptBase;
import com.gmail.inverseconduit.chat.MessageListener;
import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * JavaBot pilot class. This class is subscribed to a {@link com.gmail.inverseconduit.ChatInterface ChatInterface}.
 *
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 * @author Vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public final class JavaBot extends AbstractBot {

    private static final Logger         LOGGER        = Logger.getLogger(JavaBot.class.getName());

    private final Binding               scriptBinding = new Binding();

    private final GroovyShell           groovyShell;

    private final CompilerConfiguration groovyConfig;

    private final GroovyClassLoader     groovyLoader;

    private final ExecutorService       threadPool    = Executors.newCachedThreadPool();

    public JavaBot() {
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

    @Override
    public void processMessages() {
        LOGGER.finest("processing messages");
        //TODO: handle all messages currently in queue
        final ChatMessage message = messageQueue.poll();
        System.out.println(message.toString());
        threadPool.execute(() -> {
            for (MessageListener subscriber : getSubscribers())
                subscriber.onMessage(this, message);
        });
    }
}
