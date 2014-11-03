package com.gmail.inverseconduit.chat;

import com.gmail.inverseconduit.SESite;

public class ChatMessage {
    private final String username;
    private final int userId;
    private final String message;
    private final SESite site;
    private final int roomId;
    private final long timeStamp;
    private final String roomName;

    public ChatMessage(SESite site, int roomId, String roomName,
                       String username, int userId, String message,long timeStamp) {
        this.site = site;
        this.roomId = roomId;
        this.roomName = roomName;
        this.username = username;
        this.userId = userId;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public static ChatMessage fromEvent(SESite site, JSONChatEvent event){
        return new ChatMessage(site, event.getRoom_id(), event.getRoom_name(),
                event.getUser_name(), event.getUser_id(), event.getContent(), event.getTime_stamp());
    }
    
    @Override
    public String toString() {
        return String.format("%s:(%s) / %s(%s): %s", site.name(),
                roomName, roomId, username, userId, message);
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public SESite getSite() {
        return site;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }
    
    public long getTimeStamp() {
        return timeStamp;
    }
}
