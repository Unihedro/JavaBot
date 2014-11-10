package com.gmail.inverseconduit;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BotConfig {

    public static final int    CLIENT_ID    = 317;
    public static final String CLIENT_KEY   = "CLIENT_KEY";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String LOGIN_EMAIL  = "LOGIN_EMAIL";
    public static final String PASSWORD     = "PASSWORD";
    /**
     * @deprecated This constant is only here for legacy purposes. Use
     *             <tt>@ListenerProperty</tt> to specify the command invocation
     *             sequence(s). Will be removed when Subscribers system is
     *             finalized.
     */
    @Deprecated
    public static final String TRIGGER      = "##";
    public static final Path   JAVADOCS_DIR = Paths.get("javadocs");

    private BotConfig() {}

}
