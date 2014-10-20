// CLASS CREATED 2014/10/19 AT 5:16:59 P.M.
// SEChat.java by Unihedron
package com.gmail.inverseconduit;

/**
 * Generates locations to the destinated address.<br>
 * SEChat @ com.gmail.inverseconduit
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
public enum SESite {
    STACK_OVERFLOW("stackoverflow"),
    STACK_EXCHANGE("stackexchange"),
    META_STACK_EXCHANGE("meta." + STACK_EXCHANGE.dir);

    private final String dir;
    private final String rootUrl;
    private final String loginUrl;

    SESite(String dir) {
        this.dir = dir;
        this.rootUrl = "https://" + dir + ".com/";
        this.loginUrl = rootUrl + "users/login";
    }

    public String urlToRoom(int id) throws IllegalArgumentException {
        if (id <= 0) throw new IllegalArgumentException("id must be a positive number.");
        return "http://chat." + dir + ".com/rooms/" + id;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }
}
