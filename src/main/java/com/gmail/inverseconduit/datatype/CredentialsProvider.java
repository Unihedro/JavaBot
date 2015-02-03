package com.gmail.inverseconduit.datatype;

/**
 * A simple interface specifying the minimum required data to authenticate the
 * bot in a login process.<br/>
 * <br/>
 * The representation happens in form of an identificator/authenticator-pair. In
 * a usual login-process these are synonymous to
 * Username and Password. As such there should be extreme care to log or
 * otherwise disclose any of
 * this information.
 * 
 * @author Vogel612<<a href="mailto:vogel612@gmx.de"
 *         >vogel612@gmx.de</a>>
 */
public interface CredentialsProvider {

    /**
     * A String representing the identificator (often a username) of the
     * represented credentials.
     * In and of itself this cannot be sufficient to impersonate someone. For a
     * proper authentication against a check the authenticator is required.
     * 
     * @return The String representation of the credentials' identificator
     */
    String getIdentificator();

    /**
     * A String representing the authenticator (often a password) of the
     * represented credentials.
     * As this represents a password, when handling this one should exercise
     * extreme care to not disclose information about it.
     * Without this the identificator is useless as it's not verified
     * 
     * @return The String representation of the credentials' authenticator
     */
    String getAuthenticator();
}
