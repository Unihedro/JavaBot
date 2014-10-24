// CLASS CREATED 2014/10/19 AT 5:16:59 P.M.
// SEChat.java by Unihedron
package com.gmail.inverseconduit;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Generates locations to the destinated address.<br>
 * SEChat @ com.gmail.inverseconduit
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
public enum SEChat {
    chatSO("stackoverflow"),
    chatSE("stackexchange"),
    chatMSE("meta." + chatSE.dir);

    private final String dir;

    SEChat(String dir) {
        this.dir = dir;
    }

    URI urlToRoom(int id) throws IllegalArgumentException {
        if (id <= 0)
            throw new IllegalArgumentException("id must be a positive number.");
        try {
            return new URI("http://chat." + dir + ".com/rooms/" + id);
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException(e.getClass().getName() + ":" + e.getMessage());
        }
    }
}
