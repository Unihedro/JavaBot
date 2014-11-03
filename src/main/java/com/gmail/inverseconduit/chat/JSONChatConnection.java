package com.gmail.inverseconduit.chat;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.gistlabs.mechanize.impl.MechanizeAgent;
import com.gmail.inverseconduit.SESite;
import com.google.gson.Gson;

public class JSONChatConnection {

    private static final String  EVENT_URL_FORMAT  = "http://chat.%s.com/chats/%d/events";

    private static final Logger  logger            = Logger.getLogger(JSONChatConnection.class.getName());

    private static final Gson    gson              = new Gson();

    private final MechanizeAgent agent;

    private boolean              isEnabled         = false;

    private final MessageRelay   seBrowser;

    public JSONChatConnection(MechanizeAgent agent, MessageRelay relay) {
        this.agent = agent;
        seBrowser = relay;
    }

    public void queryEventsFor(final SESite site, final int chatId, final String fkey) {
        logger.finest(String.format("Querying events for %s 's room %d", site, chatId));
        if ( !isEnabled)
            throw new IllegalStateException();

        HashMap<String, String> params = new HashMap<>();
        params.put("mode", "Messages");
        params.put("fkey", fkey);
        params.put("msgCount", "5"); // limits fetched messages to the last 5!
        //FIXME: Check how the "since" param works...

        String rString;
        try {
            rString = agent.post(String.format(EVENT_URL_FORMAT, site.getDomain(), chatId), params).asString();
            logger.finest("Response from chat: " + rString);
        } catch(UnsupportedEncodingException e) {
            logger.severe("Unsupported Encoding: " + e.toString());
            throw new RuntimeException(e);
        }

        JSONChatEvents events = gson.fromJson(rString, JSONChatEvents.class);
        events.site = site;

        //FIXME: decode html-special code to standard strings &quot; and stuff...
        seBrowser.handleChatEvents(events);
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
