package com.gmail.inverseconduit.scripts;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

import java.util.logging.Logger;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.gmail.inverseconduit.ScriptBase;
import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * Class to run chat Code. The relevant commands submit code from the chat to
 * this class for running.
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 * @author vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public class ScriptRunner {

    private static final Logger         LOGGER        =
                                                              Logger.getLogger(ScriptRunner.class.getName());

    private final Binding               scriptBinding = new Binding();

    private final GroovyShell           groovyShell;

    private final CompilerConfiguration groovyConfig;

    private final GroovyClassLoader     groovyLoader;

    public ScriptRunner() {
        // Groovy
        groovyConfig = new CompilerConfiguration();
        groovyConfig.setScriptBaseClass(ScriptBase.class.getName());
        // scriptBinding.setVariable("javaBot", null);
        //FIXME: we could use JavaBot for this
        groovyLoader =
                new GroovyClassLoader(this.getClass().getClassLoader(),
                    groovyConfig);
        groovyShell =
                new GroovyShell(this.getClass().getClassLoader(),
                    scriptBinding, groovyConfig);
    }

    public String evaluateGroovy(ChatMessage msg, String commandText) {
        LOGGER.info("Evaluating Groovy Script");
        Object result;
        try {
            result = groovyShell.evaluate(createCodeSource(commandText));
        } catch(CompilationFailedException ex) {
            result = "compilation failed";
        }
        LOGGER.info("Result:" + result);
        return result == null
            ? String.format(":%d :no result", msg.getMessageId())
            : String.format(":%d :%s", msg.getMessageId(), result.toString());
    }

    public void evaluateAndCache(ChatMessage msg, String commandText) {
        LOGGER.finest("Compiling class to cache it");

        groovyLoader.parseClass(createCodeSource(commandText), true);
    }

    private GroovyCodeSource createCodeSource(String commandText) {
        return new GroovyCodeSource(commandText, "UserScript", "/sandboxScript");
    }
}
