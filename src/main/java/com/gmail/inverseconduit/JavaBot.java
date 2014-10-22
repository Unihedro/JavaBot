// CLASS CREATED 2014/10/19 AT 4:41:58 P.M.
// JavaBot.java by Unihedron
package com.gmail.inverseconduit;

import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;
import com.gmail.inverseconduit.chat.StackExchangeChat;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.ArrayList;

/**
 * Procrastination: I'll fix this javadoc comment later.<br>
 * JavaBot @ com.gmail.inverseconduit
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
public class JavaBot {
    private final StackExchangeChat seChat;
    private final ArrayList<ChatMessageListener> listeners = new ArrayList<>();
    private final Binding scriptBinding = new Binding();
    private final GroovyShell groovyShell;
    private final CompilerConfiguration groovyConfig;
    private final GroovyClassLoader groovyLoader;

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

    public boolean addListener(ChatMessageListener listener) {
        return listeners.add(listener);
    }

    public boolean removeListener(ChatMessageListener listener) {
        return listeners.remove(listener);
    }

    public ArrayList<ChatMessageListener> getListeners() {
        return listeners;
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

    public void handleMessage(ChatMessage message) {
        System.out.println(message.toString());
        for(ChatMessageListener listener : listeners) {
            listener.onMessage(this, message);
        }
    }
}
