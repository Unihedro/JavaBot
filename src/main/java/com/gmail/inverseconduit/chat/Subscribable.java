package com.gmail.inverseconduit.chat;

/**
 * Interface to denote a class, that produces instances of U, that can be handled by instances of T.
 * 
 * One can subscribe and unSubscribe instances of T to recieve produced U's
 * 
 * @author Vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
public interface Subscribable<T> {

    public void subscribe(T subscriber);
    public void unSubscribe(T subscriber);
}
