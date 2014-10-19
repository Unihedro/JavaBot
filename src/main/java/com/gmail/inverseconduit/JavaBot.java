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

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            new JavaBot().go();
        } catch(IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    ConnectionManager manager = new SimpleConnectionManager();

    void go() throws IllegalStateException {
        manager.establishConnection();
    }

}
