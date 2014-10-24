package com.gmail.inverseconduit.chat;

import java.util.Arrays;
import java.util.List;

import com.gmail.inverseconduit.SESite;

public class JsonChatEvents {
    private JsonChatEvent[] events;
    SESite site;
    
    
    public List<JsonChatEvent> getEvents() {
        return Arrays.asList(events);
    }
    public SESite getSite() {
        return site;
    }
}
