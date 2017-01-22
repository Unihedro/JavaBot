package com.gmail.inverseconduit.chat;

import javax.annotation.concurrent.ThreadSafe;

import com.gmail.inverseconduit.datatype.ChatDescriptor;
import com.gmail.inverseconduit.datatype.CredentialsProvider;
import com.gmail.inverseconduit.datatype.ProviderDescriptor;

/**
 * Interface specifying least common methods for a class that acts as interface
 * to a chat.
 * <p>
 * A {@link ChatInterface} "produces"
 * {@link com.gmail.inverseconduit.datatype.ChatMessage ChatMessages},
 * subscribers must implement {@link ChatWorker}
 * </p>
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 * @author Vogel612<<a href="mailto:vogel612@gmx.de">vogel612@gmx.de</a>>
 */
@ThreadSafe
public interface ChatInterface extends Subscribable<ChatWorker>, AutoCloseable {

    /**
     * Queries the messages of the chat and enqueues messages that have changed
     * since last calling this method to subscribers.
     * 
     * @apiNote This method is designed to be called
     *          repetitively
     */
    void queryMessages();

    /**
     * Sends a message to a given chatroom.
     * 
     * @implNote This method obeys external
     *           requirements such as message length, throttling and encoding.
     * @param descriptor
     *        The chat-room to send the message to
     * @param message
     *        The message as a plain text String.
     * @return a boolean indicating success or failure to relay the message
     */
    boolean sendMessage(ChatDescriptor descriptor, String message);

    /**
     * Joins the "Chat" described in the {@link ChatDescriptor Descriptor}
     * given.
     * 
     * @implNote Only joined chats will (and can)
     *           be queried when calling {@link #queryMessages()}
     * @param descriptor
     *        The chat-room to add to the internal collection of rooms to query
     * @return a boolean indicating success or failure to join the specified
     *         room
     */
    boolean joinChat(ChatDescriptor descriptor);

    /**
     * Leaves the "Chat" described in the {@link ChatDescriptor Descriptor}
     * given.
     * <p>
     * The "Chat" will no more be queried when {@link #queryMessages()} is
     * called. Accordingly all conversations in that "Chat" will not be
     * processed anymore.
     * </p>
     * 
     * @implNote Calling {@link #joinChat(ChatDescriptor)} for the same "Chat"
     *           later does not guarantee to redeem all Conversations since the
     *           chat was left.
     * @param descriptor
     *        the chat-room to remove from the internal collection of rooms to
     *        query
     * @return a boolean indicating success or failure to leave the specified
     *         chat. Non-joined chats will also return false
     */
    boolean leaveChat(ChatDescriptor descriptor);

    /**
     * Logs in the {@link ChatInterface} against a {@link ProviderDescriptor
     * Provider} using the given
     * credentials.
     * 
     * @param descriptor
     *        The {@link ProviderDescriptor Provider} as to be used in later
     *        {@link ChatDescriptor#getProvider()} calls.
     * @param credentials
     *        The {@CredentialsProvider Credentials} to
     *        allow identification and authentication.
     * @return a boolean indicating success or failure to authenticate against
     *         the given Provider
     * @apiNote This method makes no guarantees about the transmission of given
     *          credentials over possibly insecure networks.
     * @implNote Implementing classes should strive to allow for timely garbage
     *           collection of the Credentials and not log any part of the
     *           Credentials.
     */
    boolean login(ProviderDescriptor descriptor, CredentialsProvider credentials);

    /**
     * Broadcasts a given message to all currently joined chat-rooms
     * 
     * @param message
     *        the broadcast message
     */
    void broadcast(String message);

    /**
     * Closes the {@link ChatInterface}. Any calls to public API may result in
     * undefined behavior after this method was called.
     * 
     * @implNote This method is overriden with a "NO-OP" implementation to allow
     *           slim implementations.
     */
    @Override
    default void close() throws Exception {

    }
}
