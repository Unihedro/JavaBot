package com.gmail.inverseconduit.internal;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.logging.Logger;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.ScriptBase;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.javadoc.ClassInfo;
import com.gmail.inverseconduit.javadoc.JavadocDao;
import com.gmail.inverseconduit.javadoc.JavadocZipDao;
import com.gmail.inverseconduit.javadoc.MultipleClassesFoundException;

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

    private static final JavadocDao     javadocDao;

    private final Binding               scriptBinding = new Binding();

    private final GroovyShell           groovyShell;

    private final CompilerConfiguration groovyConfig;

    private final GroovyClassLoader     groovyLoader;

    private final ChatInterface         chatInterface;

    static {
        if (Files.isDirectory(BotConfig.JAVADOCS_DIR)) {
            try {
                javadocDao = new JavadocZipDao(BotConfig.JAVADOCS_DIR);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            javadocDao = null;
        }
    }

    public ScriptRunner(ChatInterface chatInterface) {
        // Groovy
        groovyConfig = new CompilerConfiguration();
        groovyConfig.setScriptBaseClass(ScriptBase.class.getName());
        scriptBinding.setVariable("javaBot", this);
        groovyLoader = new GroovyClassLoader(this.getClass().getClassLoader(), groovyConfig);
        groovyShell = new GroovyShell(this.getClass().getClassLoader(), scriptBinding, groovyConfig);
        this.chatInterface = chatInterface;
    }

    public void evaluateGroovy(ChatMessage msg, String commandText) {
        LOGGER.finest("Evaluating Groovy Script");

        Object result = groovyShell.evaluate(createCodeSource(commandText));
        chatInterface.sendMessage(msg.getSite(), msg.getRoomId(), result.toString());
    }

    public void evaluateAndCache(ChatMessage msg, String commandText) {
        LOGGER.finest("Compiling class to cache it");

        groovyLoader.parseClass(createCodeSource(commandText), true);
        chatInterface.sendMessage(msg.getSite(), msg.getRoomId(), "Thanks, I'll remember that");
    }

    public void compileAndExecuteMain(ChatMessage msg, String commandText) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        LOGGER.finest("Compiling and executing class");

        Object gClass = groovyLoader.parseClass(createCodeSource(commandText), false);
        String result = ((Class) gClass).getMethod("main", String[].class).invoke(null, (Object) new String[] {""}).toString();

        chatInterface.sendMessage(msg.getSite(), msg.getRoomId(), result);
    }

    public void javadoc(ChatMessage msg, String commandText) {
        //FIXME: Clean this mess up..
        final String message;
        ClassInfo info;
        if (javadocDao == null) {
            message = "Sorry, I can't answer that. My Javadocs folder isn't configured!";
        }
        else {
            try {
                info = javadocDao.getClassInfo(commandText);
            } catch(MultipleClassesFoundException e) {
                StringBuilder sb = new StringBuilder("Which one do you mean?");
                for (String name : e.getClasses()) {
                    sb.append("\n    ").append(name);
                }
                message = sb.toString();
                chatInterface.sendMessage(msg.getSite(), msg.getRoomId(), message);
                return;
            } catch(IOException e) {
                message = "Whoops! Something went wrong when checking the javadocs";
                chatInterface.sendMessage(msg.getSite(), msg.getRoomId(), message);
                return;
            }
            if (info == null) {
                message = "Sorry, I never heard of that class. :(";
            }
            else {
                StringBuilder sb = new StringBuilder(info.getDescription());
                int pos = sb.indexOf("\n\n");
                if (pos >= 0) {
                    //just display the first paragraph
                    message = sb.substring(0, pos);
                }
                else {
                    message = sb.toString();
                }
            }
        }
        chatInterface.sendMessage(msg.getSite(), msg.getRoomId(), message);
    }

    private GroovyCodeSource createCodeSource(String commandText) {
        return new GroovyCodeSource(commandText, "UserScript", "/sandboxScript");
    }
}
