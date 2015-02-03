package com.gmail.inverseconduit.datatype;

import java.util.Arrays;
import java.util.List;

import com.gmail.inverseconduit.SESite;

public class JsonMessages {

    private JsonMessage[] events;

    SESite                  site;

    public List<JsonMessage> getEvents() {
        return Arrays.asList(events);
    }

    public SESite getSite() {
        return site;
    }

    public void setSite(SESite site) {
        if (null == this.site)
            this.site = site;
    }
}
