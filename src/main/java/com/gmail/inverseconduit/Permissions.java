package com.gmail.inverseconduit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Permissions {
	/**
	 * Checks if the user in a certain room is at least privileged (RO or Admin)
	 * @param uID userID to check
	 * @param rID roomID to check for roomowner privileges
	 * @param bc BotConfig
	 * @return true if privileged; false if not
	 */
	public static boolean isPrivilegedUser(Long uID, Integer rID, BotConfig bc) {
		return isAdmin(uID, bc) || bc.getRoomObjects().get(rID).getRoomOwner().contains(uID);
	}
	
	public static boolean isAdmin(Long userID, BotConfig bc) {
		return bc.getAdmins().contains(userID);
	}
	
	public static String removeAdmin(Long userID, BotConfig bc) {
		
		if (!Permissions.isAdmin(userID, bc)) {
			return "This user does not have elevated privileges!";
		}
		
		Path file = Paths.get("bot.properties");
		removePrivFromFile(file, (long) userID, "ADMINS");
		
		bc.getAdmins().remove(userID);
		
		return "User " + userID + " was removed from the list of admins.";
		
	}
	
	public static String addAdmin(Long userID, BotConfig bc) {
		if (Permissions.isAdmin(userID, bc)) {
			return "User already has elevated privileges!";
		}
		
		Path file = Paths.get("bot.properties");
		addPrivToFile(file, (long) userID, "ADMINS");
		
		bc.getAdmins().add(userID);
		
		return "User " + userID + " was added succefully as admin.";
	}

	public static boolean isBanned(Long userID, BotConfig bc) {
		return bc.getBanned().contains(userID);
	}
	
	public static String removeBanned(Long userID, BotConfig bc) {
		if  (!Permissions.isBanned(userID, bc)) {
			return "This user is not banned!";
		}
		
		Path file = Paths.get("bot.properties");
		removePrivFromFile(file, (long) userID, "BANNED");
		
		bc.getBanned().remove(userID);
		
		return "User " + userID + " was removed succefully from banned users.";
	}
	
	public static String addBanned(Long userID, BotConfig bc) {
		if (Permissions.isBanned(userID, bc)) {
			return "This user already is banned!";
		}
		
		Path file = Paths.get("bot.properties");
		addPrivToFile(file, (long) userID, "BANNED");
		
		bc.getBanned().add(userID);
		
		return "User " + userID + " was added succefully as banned user.";
	}
	/**
	 * Adds the new admin to the bot.properties file
	 * 
	 * @param propertyFile
	 *            file to add the ID to
	 * @param newAdmin
	 *            ID of new admin
	 */
	private static void addPrivToFile(Path propertyFile, Long newEntry, String role) {
		try {
			Path tempFile = Paths.get(propertyFile.toString() + ".tmp");
			Files.deleteIfExists(tempFile);

			BufferedReader br = new BufferedReader(new FileReader(propertyFile.toFile()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile.toFile()));
			String nextLine = null;
      
			boolean lineFound = false;
			
			while ((nextLine = br.readLine()) != null) {
				if (nextLine.toUpperCase().startsWith(role)) lineFound = true;
				String line = nextLine.toUpperCase().startsWith(role) ? nextLine + ", " + newEntry : nextLine;
				bw.write(line + "\n");
			}

			if (!lineFound) bw.write(role + " " + newEntry);
			br.close();
			bw.close();

			Files.deleteIfExists(propertyFile);
			Files.move(tempFile, propertyFile);

		} catch (IOException e) {
     	Logger.getAnonymousLogger().log(Level.SEVERE, "Problem adding " + role + " " + newEntry + " to property file!", e);
		}
	}

	private static void removePrivFromFile(Path propertyFile, Long newEntry, String role) {
		try {
			Path tempFile = Paths.get(propertyFile.toString() + ".tmp");
			
			Files.deleteIfExists(tempFile);
			
			BufferedReader br = new BufferedReader(new FileReader(propertyFile.toFile()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile.toFile())); 
			
			String nextLine = null;
			while((nextLine = br.readLine()) != null){
				String line = nextLine.toUpperCase().startsWith(role) ? nextLine.replace(Long.toString(newEntry), "").
						//Regex to clean up the line
						replace(", ,", ",").replaceAll(", $", "").replace("=, ", "=") : nextLine;
						bw.write(line + "\n");
			}

			br.close();
			bw.close();
			
			Files.deleteIfExists(propertyFile);
			Files.move(tempFile, propertyFile);
			
		} catch (IOException e){
			Logger.getAnonymousLogger().log(Level.SEVERE, "Problem removing " + role + " " + newEntry + " from property file!", e);
		}
	}
	
}
