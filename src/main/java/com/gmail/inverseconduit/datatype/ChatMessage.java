package com.gmail.inverseconduit.datatype;

import com.gmail.inverseconduit.SESite;

public class ChatMessage {

    // FIXME: Why is this a long, when Json has an int?
    private final long   messageId;

    private final String username;

    private final int    userId;

    private final String message;

    private final SESite site;

    private final int    roomId;

    private final String roomName;

    public ChatMessage(SESite site, int roomId, String roomName, String username, int userId, String message, long messageId) {
        this.site = site;
        this.roomId = roomId;
        this.roomName = roomName;
        this.username = username;
        this.userId = userId;
        this.message = message;
        this.messageId = messageId;
    }

    public static ChatMessage fromJsonChatEvent(final JsonMessage event, final SESite site) {
        return new ChatMessage(site, event.getRoom_id(), event.getRoom_name(), event.getUser_name(), event.getUser_id(), event.getContent(), event.getMessage_id());
    }

    @Override
    public String toString() {
        return String.format("%s:%s(%s) / %s(%s): %s", site.name(), roomName, roomId, username, userId, message);
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

    public long getMessageId() {
        return messageId;
    }
}
