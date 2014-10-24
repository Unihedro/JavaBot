// CLASS CREATED 2014/10/19 AT 4:48:27 P.M.
// SimpleConnectionManager.java by Unihedron
package com.gmail.inverseconduit;

import java.io.IOException;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import com.gmail.inverseconduit.SEChat;

class SimpleConnectionManager implements ConnectionManager {

    private static final String email = "10395287@opayq.com", password = "Polyhedron0";

    @Override
    public void establishConnection() {
        login();
        try {
            System.out.println(Request.Post(SEChat.chatSO.urlToRoom(139) + "/java").bodyForm(Form.form().add("input", "~ Hello World").build()).execute().returnContent());
        } catch(IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean login() {
        try {
            System.out.println(Request.Post("http://stackoverflow.com/users/login?returnurl=http%3a%2f%2fchat.stackoverflow.com%2frooms%2f139").bodyForm(Form.form().add("email", email).add("password", password).build()).execute().returnContent());
        } catch(IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
