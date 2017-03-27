package com.gmail.inverseconduit.commands.sets;

import java.util.logging.Logger;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.Permissions;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;

public final class ChatCommands {

	public static CommandHandle unsummonCommand(ChatInterface chatInterface, BotConfig bc) {
		return new CommandHandle.Builder("unsummon", message -> {
			if (!Permissions.isAdmin((long) message.getUserId(), bc)) {
				return "I am afraid, I cannot let you do that!!";
			} 
			SeChatDescriptor descriptor = SeChatDescriptor.buildSeChatDescriptorFrom(message);
			chatInterface.leaveChat(descriptor);
			return "*~bye, bye*";
		}).build();
	}

	public static CommandHandle summonCommand(ChatInterface chatInterface, BotConfig bc) {
		return new CommandHandle.Builder("summon", message -> {
			if (!Permissions.isAdmin((long) message.getUserId(), bc)) {
				return "I am afraid, I cannot let you do that!!";
			}
			Logger.getAnonymousLogger().info("Actually invoking summon command");
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
				return "The given site was not one of stackoverflow, stackexchange or meta";
			}
			try {
				final int targetRoom = Integer.parseInt(args[2]);
				
				if (!chatInterface.joinChat(
						new SeChatDescriptor.DescriptorBuilder(targetSite).setRoom(() -> targetRoom).build())) {
					return "Could not join room.";
				}
				return "Successfully joined room";
			} catch (NumberFormatException ex) {
				return "Could not determine roomnumber.";
			}
			
		}).build();
	}


}
