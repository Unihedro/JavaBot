package com.gmail.inverseconduit.security;

import java.security.*;

public class ScriptSecurityPolicy extends Policy {

    private static final ScriptSecurityPolicy instance = new ScriptSecurityPolicy();

    public static ScriptSecurityPolicy getInstance() {
        return instance;
    }

    @Override
    public PermissionCollection getPermissions(CodeSource codeSource) {
        Permissions p = new Permissions();
        if (codeSource.getLocation().toString().equals("file:/sandboxScript"))
            System.out.println(codeSource.getLocation().toString());
        else p.add(new AllPermission());
        return p;
    }

    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        return getPermissions(domain.getCodeSource());
    }

    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {
        if (domain.getCodeSource().getLocation().toString().equals("file:/sandboxScript"))
            // TODO This doesn't seem right, but it works.
            throw new SecurityException("You can't do that.");
        return true;
    }
}
