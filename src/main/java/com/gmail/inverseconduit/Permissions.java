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

	public static boolean isAdmin(Long userID, BotConfig bc) {
		return bc.getAdmins().contains(userID);
	}
	
	public static String removeAdmin(Long userID, BotConfig bc) {
		
		if (!Permissions.isAdmin(userID, bc)) {
			return "This user does not have elevated privileges!";
		}
		
		Path file = Paths.get("bot.properties");
		removeAdminFromFile(file, (long) userID);
		
		bc.getAdmins().remove(userID);
		
		return "User " + userID + " was removed from the list of admins.";
		
	}
	
	public static String addAdmin(Long userID, BotConfig bc) {
		if (Permissions.isAdmin(userID, bc)) {
			return "User already has elevated privileges!";
		}
		
		Path file = Paths.get("bot.properties");
		addAdminToFile(file, (long) userID);
		
		bc.getAdmins().add(userID);
		
		return "User " + userID + " was added succefully as admin.";
	}

	/**
	 * Adds the new admin to the bot.properties file
	 * 
	 * @param propertyFile
	 *            file to add the ID to
	 * @param newAdmin
	 *            ID of new admin
	 */
	private static void addAdminToFile(Path propertyFile, Long newAdmin) {
		try {
			Path tempFile = Paths.get(propertyFile.toString() + ".tmp");
			Files.deleteIfExists(tempFile);

			BufferedReader br = new BufferedReader(new FileReader(propertyFile.toFile()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile.toFile()));
			String nextLine = null;
			while ((nextLine = br.readLine()) != null) {
				String line = nextLine.toUpperCase().startsWith("ADMINS") ? nextLine + ", " + newAdmin : nextLine;
				bw.write(line + "\n");
			}

			br.close();
			bw.close();

			Files.deleteIfExists(propertyFile);
			Files.move(tempFile, propertyFile);

		} catch (IOException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Problem adding " + newAdmin + " to property file!", e);
		}
	}

	private static void removeAdminFromFile(Path propertyFile, Long newAdmin) {
		try {
			Path tempFile = Paths.get(propertyFile.toString() + ".tmp");
			
			Files.deleteIfExists(tempFile);
			
			BufferedReader br = new BufferedReader(new FileReader(propertyFile.toFile()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile.toFile())); 
			
			String nextLine = null;
			while((nextLine = br.readLine()) != null){
				String line = nextLine.toUpperCase().startsWith("ADMINS") ? nextLine.replace(Long.toString(newAdmin), "").
						//Regex to clean up the line
						replace(", ,", ",").replaceAll(", $", "").replace("=, ", "=") : nextLine;
						bw.write(line + "\n");
			}

			br.close();
			bw.close();
			
			Files.deleteIfExists(propertyFile);
			Files.move(tempFile, propertyFile);
			
		} catch (IOException e){
			Logger.getAnonymousLogger().log(Level.SEVERE, "Problem removing " + newAdmin + " from property file!", e);
		}
	}
	
}
