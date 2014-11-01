package com.gmail.inverseconduit.security;

import java.security.Permission;

public final class ScriptSecurityManager extends SecurityManager {

    private static final ScriptSecurityManager instance = new ScriptSecurityManager();

    public static ScriptSecurityManager getInstance() {
        return instance;
    }

    private ScriptSecurityManager() {}

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (context.getClass().getName().equals("UserScript"))
            throw new SecurityException("You can't do that.");
    }

}
