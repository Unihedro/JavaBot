// CLASS CREATED 2014/10/19 AT 4:41:58 P.M.
// JavaBot.java by Unihedron
package com.gmail.inverseconduit.bot;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.ScriptBase;
import com.gmail.inverseconduit.chat.ChatMessage;
import com.gmail.inverseconduit.chat.ChatMessageListener;

/**
 * Procrastination: I'll fix this javadoc comment later.<br>
 * JavaBot @ com.gmail.inverseconduit
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
public final class JavaBot extends AbstractBot {
	private final Binding scriptBinding = new Binding();
	private final GroovyShell groovyShell;
	private final CompilerConfiguration groovyConfig;
	private final GroovyClassLoader groovyLoader;
	private final ExecutorService threadPool = Executors.newCachedThreadPool();

	public JavaBot() {
		// Groovy
		groovyConfig = new CompilerConfiguration();
		groovyConfig.setScriptBaseClass(ScriptBase.class.getName());
		scriptBinding.setVariable("javaBot", this);
		groovyLoader = new GroovyClassLoader(JavaBot.class.getClassLoader(),
				groovyConfig);
		groovyShell = new GroovyShell(this.getClass().getClassLoader(),
				scriptBinding, groovyConfig);
	}

	public GroovyShell getGroovyShell() {
		return groovyShell;
	}

	public GroovyClassLoader getGroovyLoader() {
		return groovyLoader;
	}

	@Override
	public boolean addListener(ChatMessageListener listener) {
		return listeners.add(listener);
	}

	@Override
	public boolean removeListener(ChatMessageListener listener) {
		return listeners.remove(listener);
	}

	@Override
	public ArrayList<ChatMessageListener> getListeners() {
		return listeners;
	}

	public boolean login(SESite site, String username, String password) {
		return relay.loginWithEmailAndPass(site, username, password);
	}

	public boolean joinChat(SESite site, int chatId) {
		return relay.joinChat(site, chatId);
	}

	public boolean sendMessage(SESite site, int chatId, String message) {
		return relay.sendMessage(site, chatId, message);
	}

	@Override
	public synchronized void processMessages() {
		try {
			final ChatMessage message = messageQueue.take();
			threadPool.execute(() -> {
				for (ChatMessageListener listener : listeners) {
					listener.onMessage(this, message);
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
