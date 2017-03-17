package com.gmail.inverseconduit.commands.sets;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.Permissions;
import com.gmail.inverseconduit.commands.CommandHandle;

public class AdminCommands {

	public static CommandHandle addAdminCommand(BotConfig bc) {
		return new CommandHandle.Builder("addAdmin", message -> {

			if (! Permissions.isAdmin((long) message.getUserId(), bc)) {
				return "I am afraid, I cannot let you do that";
			}
		
			String[] args = message.getMessage().trim().split(" ");
			try {
				Long newUserId = Long.parseLong(args[1]);
				return Permissions.addAdmin(newUserId, bc);
			} catch (NumberFormatException e) {
				return "Could not understand userID!";
			}
			
		}).build();
	}

	public static CommandHandle removeAdminCommand(BotConfig bc) {
		return new CommandHandle.Builder("removeAdmin", message -> {

			if (! Permissions.isAdmin((long) message.getUserId(), bc)) {
				return "I am afraid, I cannot let you do that!";
			}
			
			String[] args = message.getMessage().trim().split(" ");
			try {
				Long remID = Long.parseLong(args[1]);
				return Permissions.removeAdmin(remID, bc);

			} catch (NumberFormatException e) {
				return "Could not understand userID!";
			}
			
		}).build();
	}

	
	
	
}