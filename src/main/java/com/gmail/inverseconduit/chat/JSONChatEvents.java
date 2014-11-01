package com.gmail.inverseconduit.chat;

import java.util.Arrays;
import java.util.List;

import com.gmail.inverseconduit.SESite;

public class JSONChatEvents {

    private JSONChatEvent[] events;

    SESite                  site;

    public List<JSONChatEvent> getEvents() {
        return Arrays.asList(events);
    }

    public SESite getSite() {
        return site;
    }
}
