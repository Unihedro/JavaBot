package com.gmail.inverseconduit.chat;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.gistlabs.mechanize.impl.MechanizeAgent;
import com.gmail.inverseconduit.SESite;
import com.google.gson.Gson;

public class JsonChatConnection {

	private static final String EVENT_URL_FORMAT = "http://chat.%s.com/chats/%d/events";

	private static final Logger logger = Logger
			.getLogger(JsonChatConnection.class.getName());
	private static final Gson gson = new Gson();

	private final MechanizeAgent agent;
	private boolean isEnabled = false;
	private final MessageRelay seBrowser;

	public JsonChatConnection(MechanizeAgent agent, MessageRelay relay) {
		this.agent = agent;
		this.seBrowser = relay;
	}

	public void queryEventsFor(final SESite site, final int chatId,
			final String fkey) {
		logger.finest(String.format("querying events for %s 's room %d",site, chatId));
		if (!isEnabled) {
			throw new IllegalStateException();
		}

		Map<String, String> params = new HashMap<>();
		params.put("mode", "Messages");
		params.put("fkey", fkey);

		String rString;
		try {
			rString = agent.post(
					String.format(EVENT_URL_FORMAT, site.getDomain(), chatId),
					params).asString();
		} catch (UnsupportedEncodingException e) {
			logger.severe("Unsupported Encoding: " + e.toString());
			throw new RuntimeException(e);
		}

		JsonChatEvents events = gson.fromJson(rString, JsonChatEvents.class);
		events.site = site;

		seBrowser.handleChatEvents(events);
	}

	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}
}
