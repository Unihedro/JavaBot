package com.gmail.inverseconduit.chat;

import javax.annotation.concurrent.ThreadSafe;

import com.gmail.inverseconduit.datatype.ChatDescriptor;
import com.gmail.inverseconduit.datatype.CredentialsProvider;
import com.gmail.inverseconduit.datatype.ProviderDescriptor;

/**
 * Interface specifying least common methods for a class that acts as interface
 * to a chat.
 * The Instances of this class are supposed to be threadsafe.
 * A ChatInterface currently produces
 * {@link com.gmail.inverseconduit.datatype.ChatMessage ChatMessages},
 * subscribers must implement {@link ChatWorker}
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 * @author Vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
@ThreadSafe
public interface ChatInterface extends Subscribable<ChatWorker> {

    /**
     * Queries the messages of the chat. This method is designed to be called
     * repetitively and enqueues Messages to the subscribers as maintained
     * internally per the contract of {@link Subscribable}
     */
    void queryMessages();

    /**
     * Sends a message to a given chatroom. This method obeys external
     * requirements such as message length, throttling and encodings.
     * 
     * @param descriptor
     *        The chat-room to send the message to
     * @param message
     *        The message as a plain text String.
     * @return a boolean indicating success or failure to relay the message
     */
    boolean sendMessage(ChatDescriptor descriptor, String message);

    /**
     * Joins a Chat as described in the descriptor given. Only joined chats will
     * be queried when calling {@link #queryMessages()}
     * 
     * @param descriptor
     *        The chat-room to add to the internal collection of rooms to query
     * @return a boolean indicating success or failure to join the specified
     *         room
     */
    boolean joinChat(ChatDescriptor descriptor);

    /**
     * leaves a Chat as described in the descriptor given. Removes the chat from
     * the list of rooms to query when calling {@link #queryMessages()}. Any
     * messages / commands called from the given chat will be ignored until the
     * chat is joined again
     * 
     * @param descriptor
     *        the chat-room to remove from the internal collection of rooms to
     *        query
     * @return a boolean indicating success or failure to leave the specified
     *         chat. Non-joined chats will also return false
     */
    boolean leaveChat(ChatDescriptor descriptor);

    /**
     * logs in the ChatInterface against a provider using the given credentials.
     * 
     * @param descriptor
     *        The Provider as to be used in later
     *        {@link ChatDescriptor#getProvider()} calls.
     * @param credentials
     *        The Credentials to allow identification and authentication using
     *        {@link CredentialsProvider#getIdentificator()} and
     *        {@link CredentialsProvider#getAuthenticator()}
     * @return a boolean indicating success or failure to authenticate against
     *         the given Provider
     */
    boolean login(ProviderDescriptor descriptor, CredentialsProvider credentials);

    /**
     * Broadcasts a given message to all currently joined chat-rooms as
     * maintained in the internal collection like used in
     * {@link #queryMessages()}
     * 
     * @param message
     *        the broadcast message
     */
    void broadcast(String message);
}
