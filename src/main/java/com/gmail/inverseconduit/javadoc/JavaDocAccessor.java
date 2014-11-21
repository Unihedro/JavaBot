package com.gmail.inverseconduit.javadoc;

import java.io.IOException;
import java.nio.file.Files;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.datatype.ChatMessage;


public class JavaDocAccessor {
    
    private final JavadocDao javadocDao;
    private final ChatInterface chatInterface;
    
    public JavaDocAccessor (ChatInterface chatInterface) {
        this.chatInterface = chatInterface;
        
        if (Files.isDirectory(BotConfig.JAVADOCS_DIR)) {
            try {
                javadocDao = new JavadocZipDao(BotConfig.JAVADOCS_DIR);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            javadocDao = null;
        }
    }

    public void javadoc(ChatMessage msg, String commandText) {
        //FIXME: Clean this mess up..
        final String message;
        ClassInfo info;
        if (javadocDao == null) {
            message = "Sorry, I can't answer that. My Javadocs folder isn't configured!";
        }
        else {
            try {
                info = javadocDao.getClassInfo(commandText);
            } catch(MultipleClassesFoundException e) {
                StringBuilder sb = new StringBuilder("Which one do you mean?");
                for (String name : e.getClasses()) {
                    sb.append("\n    ").append(name);
                }
                message = sb.toString();
                chatInterface.sendMessage(msg.getSite(), msg.getRoomId(), message);
                return;
            } catch(IOException e) {
                message = "Whoops! Something went wrong when checking the javadocs";
                chatInterface.sendMessage(msg.getSite(), msg.getRoomId(), message);
                return;
            }
            if (info == null) {
                message = "Sorry, I never heard of that class. :(";
            }
            else {
                StringBuilder sb = new StringBuilder(info.getDescription());
                int pos = sb.indexOf("\n\n");
                if (pos >= 0) {
                    //just display the first paragraph
                    message = sb.substring(0, pos);
                }
                else {
                    message = sb.toString();
                }
            }
        }
        chatInterface.sendMessage(msg.getSite(), msg.getRoomId(), message);
    }
}
