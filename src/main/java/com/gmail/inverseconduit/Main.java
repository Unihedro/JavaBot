package com.gmail.inverseconduit;

import java.security.Policy;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static void main(String[] args) {
        // HtmlUnit didn't properly clean up, so we have to
        disabledLoggers.forEach(l -> l.setLevel(Level.OFF));

        //sandbox this ...
        Policy.setPolicy(ScriptSecurityPolicy.getInstance());
        System.setSecurityManager(ScriptSecurityManager.getInstance());

        Program p = new Program();
        p.startup();
    }
}
