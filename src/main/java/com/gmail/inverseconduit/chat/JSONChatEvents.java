package com.gmail.inverseconduit.chat;

import com.gmail.inverseconduit.SESite;

import java.util.ArrayList;

public class JSONChatEvents {
    private ArrayList<JSONChatEvent> e;
    SESite site;
    public ArrayList<JSONChatEvent> getEvents() {
        return e;
    }
    public SESite getSite() {
        return site;
    }
}
