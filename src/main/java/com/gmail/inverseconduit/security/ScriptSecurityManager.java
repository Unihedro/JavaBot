package com.gmail.inverseconduit.security;

import java.security.Permission;

public class ScriptSecurityManager extends SecurityManager {
    private static ScriptSecurityManager instance = new ScriptSecurityManager();

    public static ScriptSecurityManager getInstance() {
        return instance;
    }

    private ScriptSecurityManager(){
    }

    public void checkPermission(Permission p, Object c) {
        if(c.getClass().getName().equals("UserScript")) {
            throw new SecurityException("You can't do that.");
        }
    }

}
