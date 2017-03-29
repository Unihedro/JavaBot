package com.gmail.inverseconduit.datatype;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.utils.SourceCodeParser;

public class Room {

	private int roomID;
	private String roomName;
	private List<Long> roomOwner;
	private SESite site;

	public int getRoomID() {
		return roomID;
	}

	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public List<Long> getRoomOwner() {
		return roomOwner;
	}

	public void setRoomOwner(List<Long> roomOwner) {
		this.roomOwner = roomOwner;
	}

	public void setSite(SESite site) {
		this.site = site;
	}
	
	public SESite getSite() {
		return site;
	}
	
	public static Room createRoom(int ID, SESite site, BotConfig bc) {
		if (fileExistsForRoom(ID, site)) {
			return new Room(Paths.get("./privilege/room" + ID + site.getDir() + ".txt"), site);
		}
		
		Room room = new Room(ID, site, bc);
		createRoomFile(room);
		return room;
	}
	
	private static boolean fileExistsForRoom(int ID, SESite site) {
		return Files.exists(Paths.get("./privileges/room" + ID + site.getDir() + ".txt"));
	}
	
	private Room(Path roomOwnerFile, SESite site) { //fileStructure -> First line room ID, Second line room name, following lines are roomowners
		try {
			this.site = site;
			BufferedReader br = new BufferedReader(new FileReader(roomOwnerFile.toFile()));
			
			this.roomID = Integer.parseInt(br.readLine());
			this.roomName = br.readLine();
			
			List<Long> roomOwners = new ArrayList<>();
			String line = null;
			while ((line = br.readLine()) != null) {
				roomOwners.add(Long.parseLong(line));
			}
			
			this.roomOwner = roomOwners;
			
			br.close();
		} catch (IOException ioe) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Could not read roomfile " + roomOwnerFile.toString(), ioe);
		}
	}
	
	private Room(int ID, SESite site, BotConfig bc) { //creates the room with the information from the DOM of the info website
		this.site = site;
		
		String url = site.getDescription() + "/rooms/info/" + ID;
		SourceCodeParser scp = new SourceCodeParser(url, "owner-user-(\\d+)");
		List<String> roomOwner = scp.parse();
		
		List<Long> rO = new ArrayList<>();
		for (String roStr : roomOwner) {
			rO.add(Long.parseLong(roStr.split("-")[2]));
		}
		
		this.roomOwner = rO;
		this.roomID = ID;

		scp.setSearch("</div>.*<h1> (.*)</h1>");
		
		for (String str : scp.parse()) {
			this.roomName = str.split("<h1>")[2].replace("</h1>", "").trim();
		}
	}

	private static void createRoomFile(Room room) {
		
		String dir = room.getSite().getDir();
		Path roomFile = Paths.get("./privileges/room" + room.getRoomID() + dir + ".txt");
		
		try {
			if (!Files.exists(Paths.get("./privileges/"))) Files.createDirectories(Paths.get("./privileges/"));

			Files.createFile(roomFile);
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(roomFile.toFile()));
			
			bw.write(Integer.toString(room.getRoomID()) + "\n");
			bw.write(room.getRoomName() + "\n");
			for (Long roomOwner : room.getRoomOwner()) {
				bw.write(Long.toString(roomOwner) + "\n");
			}
			
			bw.close();
		} catch (IOException ioe) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Problem creating the room file for room " + room.getRoomID() + dir, ioe);
		}
		
		
		
	}
	
}
