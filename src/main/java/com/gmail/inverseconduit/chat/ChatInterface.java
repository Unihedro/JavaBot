package com.gmail.inverseconduit.chat;

import com.gmail.inverseconduit.SESite;
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
public interface ChatInterface extends Subscribable<ChatWorker> {

    void queryMessages();

    @Deprecated
    boolean sendMessage(SESite site, int chatId, String message);

    @Deprecated
    boolean joinChat(SESite site, int chatId);
    
    @Deprecated
    boolean leaveChat(SESite site, int chatId);

    @Deprecated
    boolean login(SESite site, String email, String password);

    boolean sendMessage(ChatDescriptor descriptor, String message);
    
    boolean joinChat(ChatDescriptor descriptor);
    
    boolean leaveChat(ChatDescriptor descriptor);
    
    boolean login (ProviderDescriptor descriptor, CredentialsProvider credentials);
    
    void broadcast(String message);
}
