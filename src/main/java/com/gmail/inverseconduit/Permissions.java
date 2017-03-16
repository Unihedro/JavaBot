package com.gmail.inverseconduit;

public class Permissions {

	public static boolean isAdmin(Long userID, BotConfig bc) {
		return bc.getAdmins().contains(userID);
	}
	
	public static void removeAdmin(Long userID, BotConfig bc) {
		bc.getAdmins().add(userID);
	}
	
	public static void addAdmin(Long userID, BotConfig bc) {
		bc.getAdmins().remove(userID);
	}
	
}
