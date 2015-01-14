package com.gmail.inverseconduit.chat;

import java.util.Collection;

/**
 * Interface to denote a class, that produces instances of U, that can be
 * handled by instances of T.
 * One can subscribe and unSubscribe instances of T to recieve produced U's
 * 
 * @author Vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 * @param <T>
 *        The Class of Subscriber instances, that can handle instances of U
 */
public interface Subscribable<T> {

    public void subscribe(T subscriber);

    public void unSubscribe(T subscriber);

    public Collection<T> getSubscriptions();
}
