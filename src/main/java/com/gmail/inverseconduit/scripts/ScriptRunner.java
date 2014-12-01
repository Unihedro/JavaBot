package com.gmail.inverseconduit.scripts;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

import java.util.logging.Logger;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.gmail.inverseconduit.ScriptBase;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;

/**
 * Class to run chat Code. The relevant commands submit code from the chat to
 * this class for running.
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 * @author vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public class ScriptRunner {

    private static final Logger         LOGGER        = Logger.getLogger(ScriptRunner.class.getName());

    private final Binding               scriptBinding = new Binding();

    private final GroovyShell           groovyShell;

    private final CompilerConfiguration groovyConfig;

    private final GroovyClassLoader     groovyLoader;

    private final ChatInterface         chatInterface;

    public ScriptRunner(ChatInterface chatInterface) {
        // Groovy
        groovyConfig = new CompilerConfiguration();
        groovyConfig.setScriptBaseClass(ScriptBase.class.getName());
        scriptBinding.setVariable("javaBot", chatInterface);
        groovyLoader = new GroovyClassLoader(this.getClass().getClassLoader(), groovyConfig);
        groovyShell = new GroovyShell(this.getClass().getClassLoader(), scriptBinding, groovyConfig);
        this.chatInterface = chatInterface;
    }

    public void evaluateGroovy(ChatMessage msg, String commandText) {
        LOGGER.finest("Evaluating Groovy Script");

        Object result = groovyShell.evaluate(createCodeSource(commandText));
        chatInterface.sendMessage(SeChatDescriptor.buildSeChatDescriptorFrom(msg), result == null
            ? "[tag:groovy]: no result"
            : "[tag:groovy]: " + result.toString());
    }

    public void evaluateAndCache(ChatMessage msg, String commandText) {
        LOGGER.finest("Compiling class to cache it");

        groovyLoader.parseClass(createCodeSource(commandText), true);
        chatInterface.sendMessage(SeChatDescriptor.buildSeChatDescriptorFrom(msg), "Thanks, I'll remember that");
    }

    private GroovyCodeSource createCodeSource(String commandText) {
        return new GroovyCodeSource(commandText, "UserScript", "/sandboxScript");
    }
}
