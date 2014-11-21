package com.gmail.inverseconduit.chat.commands;

import static com.gmail.inverseconduit.BotConfig.TRIGGER;

import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.commands.CommandHandle;


public final class ChatCommands {
    public static  CommandHandle unsummonCommand(ChatInterface chatInterface) {
        return new CommandHandle.Builder("unsummon", s -> {
            return s.trim().equals(TRIGGER + "unsummon");
        }, message -> {
            chatInterface.sendMessage(message.getSite(), message.getRoomId(), "*~bye, bye*");
            chatInterface.leaveChat(message.getSite(), message.getRoomId());
        }).build();
    }
    
    public static CommandHandle summonCommand(ChatInterface chatInterface) {
        return new CommandHandle.Builder("summon", s -> {
            return s.trim().startsWith(TRIGGER) && s.trim().matches("/.*summon (stack(overflow|exchange)|meta) [\\d]{1,6}/gi");
        }, message -> {
            String[] args = message.getMessage().trim().split(" ");
            final SESite targetSite;
            switch (args[1].toLowerCase()) {
            case "stackoverflow":
                targetSite = SESite.STACK_OVERFLOW;
                break;
            case "stackexchange":
                targetSite = SESite.STACK_EXCHANGE;
                break;
            case "meta":
                targetSite = SESite.META_STACK_EXCHANGE;
                break;
            default:
                chatInterface.sendMessage(message.getSite(), message.getRoomId(), "The given site was not one of stackoverflow, stackexchange or meta");
                return;
            }
            try {
                int targetRoom = Integer.parseInt(args[2]);
                chatInterface.joinChat(targetSite, targetRoom);
            } catch(NumberFormatException ex) {
                chatInterface.sendMessage(message.getSite(), message.getRoomId(), "Could not determine roomnumber.");
            }
        }).build();
    }

}
