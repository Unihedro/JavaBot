package com.gmail.inverseconduit.chat.commands;

import java.util.logging.Logger;

import com.gmail.inverseconduit.AppContext;
import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;

public final class ChatCommands {

    private static final BotConfig config = AppContext.INSTANCE.get(BotConfig.class);

    public static CommandHandle unsummonCommand(ChatInterface chatInterface) {
        return new CommandHandle.Builder("unsummon", s -> {
            return s.trim().equals(config.getTrigger() + "unsummon");
        }, message -> {
            SeChatDescriptor descriptor = SeChatDescriptor.buildSeChatDescriptorFrom(message);
            chatInterface.sendMessage(descriptor, "*~bye, bye*");
            chatInterface.leaveChat(descriptor);
        }).build();
    }

    public static CommandHandle summonCommand(ChatInterface chatInterface) {
        return new CommandHandle.Builder("summon", s -> {
            return s.trim().startsWith(config.getTrigger()) && s.trim().matches(".*summon (stack(overflow|exchange)|meta) [0-9]{1,6}");
        }, message -> {
            Logger.getAnonymousLogger().info("Actually invoking summon command");
            SeChatDescriptor callingRoomDescriptor = SeChatDescriptor.buildSeChatDescriptorFrom(message);
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
                chatInterface.sendMessage(callingRoomDescriptor, "The given site was not one of stackoverflow, stackexchange or meta");
                return;
            }
            try {
                int targetRoom = Integer.parseInt(args[2]);
                if ( !chatInterface.joinChat(new SeChatDescriptor.DescriptorBuilder(targetSite).setRoom(() -> targetRoom).build())) {
                    chatInterface.sendMessage(callingRoomDescriptor, "Could not join room.");
                }
            } catch(NumberFormatException ex) {
                chatInterface.sendMessage(callingRoomDescriptor, "Could not determine roomnumber.");
            }
        }).build();
    }

}
