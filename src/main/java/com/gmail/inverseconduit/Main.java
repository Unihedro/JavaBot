package com.gmail.inverseconduit;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Policy;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmail.inverseconduit.bot.Program;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;

public class Main {

    // Prevents GC of loggers before htmlUnit can get them again...
    private static final Set<Logger> disabledLoggers = new HashSet<>();

    static {
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter"));
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.DefaultCssErrorHandler"));
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl"));
        disabledLoggers.add(Logger.getLogger("com.gargoylesoftware.htmlunit.html.InputElementFactory"));
    }

    public static void main(String[] args) throws Exception {
        // HtmlUnit didn't properly clean up, so we have to
        disabledLoggers.forEach(l -> l.setLevel(Level.OFF));

        //sandbox this ...
        Policy.setPolicy(ScriptSecurityPolicy.getInstance());
        System.setSecurityManager(ScriptSecurityManager.getInstance());
        
        BotConfig config = loadConfig();
        AppContext.INSTANCE.add(config);

        Program p = new Program();
        p.startup();
    }
    
    private static BotConfig loadConfig() throws IOException{
    	Path file = Paths.get("bot.properties");
    	Properties properties = new Properties();
    	try (Reader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))){
    		properties.load(reader);
    	}
    	return new BotConfig(properties);
    }
}
