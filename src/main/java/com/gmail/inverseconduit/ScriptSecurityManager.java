package com.gmail.inverseconduit;

import java.security.*;

public class ScriptSecurityManager extends SecurityManager {

    private class SecurityPolicy extends Policy {
        @Override
        public PermissionCollection getPermissions(CodeSource codeSource) {
            Permissions p = new Permissions();
            if(codeSource.getLocation().toString().equals("file:/sandboxScript")) {
                System.out.println("Denying sandbox permissions...");
                return p;
            }
            p.add(new AllPermission());
            return p;
        }
    }

    public ScriptSecurityManager() {
        SecurityPolicy policy = new SecurityPolicy();
        Policy.setPolicy(policy);
    }


}
