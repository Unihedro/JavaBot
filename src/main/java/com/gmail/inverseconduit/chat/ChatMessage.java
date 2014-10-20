package com.gmail.inverseconduit.chat;

import com.gmail.inverseconduit.SESite;

public class ChatMessage {
    private final String displayName;
    private final String userId;
    private final String message;
    private final SESite site;
    private final int roomId;
    private final String roomName;

    public ChatMessage(SESite site, int roomId, String roomName,
                       String displayName, String userId, String message) {
        this.site = site;
        this.roomId = roomId;
        this.roomName = roomName;
        this.displayName = displayName;
        this.userId = userId;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("%s:%s(%s) / %s(%s): %s", site.name(),
                roomName, roomId, displayName, userId, message);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUserId() {
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
}
