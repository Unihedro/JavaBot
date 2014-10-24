package com.gmail.inverseconduit.chat;

public class JsonChatEvent {
    private int event_type;
    private long time_stamp;
    private String content;
    private int id;
    private int user_id;
    private String user_name;
    private int room_id;
    private String room_name;
    private int message_id;

    public long getTime_stamp() {
        return time_stamp;
    }

    public int getEvent_type() {
        return event_type;
    }

    public String getContent() {
        return content;
    }

    public int getId() {
        return id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public int getRoom_id() {
        return room_id;
    }

    public String getRoom_name() {
        return room_name;
    }

    public int getMessage_id() {
        return message_id;
    }
}
