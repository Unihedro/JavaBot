// CLASS CREATED 2014/10/19 AT 4:41:58 P.M.
// JavaBot.java by Unihedron
package com.gmail.inverseconduit;

/**
 * Procrastination: I'll fix this javadoc comment later.<br>
 * JavaBot @ com.gmail.inverseconduit
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
public class JavaBot {
    public static void main(String[] args) {
    	ConnectionManager manager = new SimpleConnectionManager("10395287@opayq.com", "Polyhedron0", 139, "java");
    	JavaBot javaBot = new JavaBot(manager);
    	javaBot.go();
    }
    
    private final ConnectionManager manager;
    
    public JavaBot(ConnectionManager manager){
    	this.manager = manager;
    }

    public void go() {
        manager.establishConnection();
    }
}