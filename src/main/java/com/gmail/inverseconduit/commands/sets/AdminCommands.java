package com.gmail.inverseconduit.commands.sets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.commands.CommandHandle;

public class AdminCommands {

	public static CommandHandle addAdminCommand(BotConfig bc) {
		return new CommandHandle.Builder("addAdmin", message -> {

			if (isElevatedUser(message.getUserId(), bc)) {
				String[] args = message.getMessage().trim().split(" ");
				try {
					Integer newUserId = Integer.parseInt(args[1]);
					if (!bc.getAdmins().contains(newUserId))
						bc.getAdmins().add(newUserId);
					else return "User already has elevated privileges!";
					Path file = Paths.get("bot.properties");
					addAdminToFile(file, newUserId);
					return "Added userID " + newUserId + " successful!";
				} catch (NumberFormatException e) {
					return "Could not understand userID!";
				}
			}
			return "I am afraid, I cannot let you do that";

		}).build();
	}

	public static CommandHandle removeAdminCommand(BotConfig bc) {
		return new CommandHandle.Builder("removeAdmin", message -> {

			if (isElevatedUser(message.getUserId(), bc)) {

				String[] args = message.getMessage().trim().split(" ");
				try {
					Integer remID = Integer.parseInt(args[1]);
					if (!bc.getAdmins().contains(remID)) 
						return "This user does not have elevated privileges!";
					
					bc.getAdmins().remove((Object) remID); //Object cast to make sure it does not get interpreted as index.
					
					Path file = Paths.get("bot.properties");
					removeAdminFromFile(file, remID);
					
					return "Removed " + remID + " sucessful";

				} catch (NumberFormatException e) {
					return "Could not understand userID!";
				}

			}

			return "I am afraid, I cannot let you do that!";
		}).build();
	}

	/**
	 * Iterates over the list of admins given by the current BotConfig to check
	 * if a user is elevated
	 * 
	 * @param uID
	 *            userID to check
	 * @param bc
	 *            BotConfig
	 * @return true if elevated; false if not
	 */
	public static boolean isElevatedUser(Integer uID, BotConfig bc) {
		boolean elevatedUser = false;
		System.out.println(uID);
		for (Integer adID : bc.getAdmins()) {
			System.out.println(adID);
			elevatedUser = adID.equals(uID) ? true : elevatedUser;
		}
		return elevatedUser;
	}

	/**
	 * Adds the new admin to the bot.properties file
	 * 
	 * @param propertyFile
	 *            file to add the ID to
	 * @param newAdmin
	 *            ID of new admin
	 */
	private static void addAdminToFile(Path propertyFile, Integer newAdmin) {
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
			System.out.println("------------------------------------------------------------------------");
			System.out.println("PROBLEM ADDING ADMIN! INVESTIGATION STRONGLY ADVISED!");
			System.out.println("------------------------------------------------------------------------");
		}
	}

	private static void removeAdminFromFile(Path propertyFile, Integer newAdmin) {
		try {
			Path tempFile = Paths.get(propertyFile.toString() + ".tmp");
			
			Files.deleteIfExists(tempFile);
			
			BufferedReader br = new BufferedReader(new FileReader(propertyFile.toFile()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile.toFile())); 
			
			String nextLine = null;
			while((nextLine = br.readLine()) != null){
				String line = nextLine.toUpperCase().startsWith("ADMINS") ? nextLine.replace(Integer.toString(newAdmin), "").
						//Regex to clean up the line
						replace(", ,", ",").replaceAll(", $", "").replace("=, ", "=") : nextLine;
						bw.write(line + "\n");
			}

			br.close();
			bw.close();
			
			Files.deleteIfExists(propertyFile);
			Files.move(tempFile, propertyFile);
			
		} catch (IOException e){
			System.out.println("------------------------------------------------------------------------");
			System.out.println("PROBLEM REMOVING ADMIN! INVESTIGATION STRONGLY ADVISED!");
			System.out.println("------------------------------------------------------------------------");
		}
	}
	
	
}