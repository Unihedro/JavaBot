package com.gmail.inverseconduit.chat;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.StackExchangeBrowser;
import com.google.gson.Gson;

import java.io.IOException;

public class JSONChatConnection extends WebConnectionWrapper {
    private boolean isEnabled = false;
    private StackExchangeBrowser seBrowser;
    private Gson gson = new Gson();

    public JSONChatConnection(WebClient webClient, StackExchangeBrowser seBrowser) throws IllegalArgumentException {
        super(webClient);
        this.seBrowser = seBrowser;
    }

    @Override
    public WebResponse getResponse(WebRequest request) throws IOException {
        WebResponse response = super.getResponse(request);
        if(isEnabled) try {
            String rString = response.getContentAsString();
            String jsonString = rString.substring(rString.indexOf(":") + 1, rString.lastIndexOf("}"));
            JSONChatEvents events = gson.fromJson(jsonString, JSONChatEvents.class);
            events.site = SESite.fromUrl(request.getUrl());
            seBrowser.handleChatEvents(events);
        } catch (Exception ignored) {
        }
        return response;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
