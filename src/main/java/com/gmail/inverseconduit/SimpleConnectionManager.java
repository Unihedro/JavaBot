// CLASS CREATED 2014/10/19 AT 4:48:27 P.M.
// SimpleConnectionManager.java by Unihedron
package com.gmail.inverseconduit;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;

class SimpleConnectionManager implements ConnectionManager {
	private final String email, password, roomName;
	private final URI chatRoomUrl;
    
    public SimpleConnectionManager(String email, String password, int roomNumber, String roomName){
    	this.email = email;
    	this.password = password;
    	this.roomName = roomName;
    	chatRoomUrl = SEChat.chatSO.urlToRoom(roomNumber);
    }
    
    @Override
    public void establishConnection() {
        if (!login()){
        	System.out.println("Invalid login credentials!");
        	return;
        }
        
        try {
        	Request request = Request.Post(chatRoomUrl + "/" + roomName);
        	List<NameValuePair> body = Form.form().add("input", "~ Hello World").build();
        	request.bodyForm(body);
        	
        	Response response = request.execute();
        	System.out.println(response.returnContent());
        } catch(IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean login() {
        try {
        	URIBuilder uri = new URIBuilder("http://stackoverflow.com/users/login");
        	uri.addParameter("returnurl", chatRoomUrl.toString());
        	Request request = Request.Post(uri.build());
        	
        	List<NameValuePair> body = Form.form().add("email", email).add("password", password).build();
        	request.bodyForm(body);
        	
        	Response response = request.execute();
        	System.out.println(response.returnContent());
        	
        	return true;
        } catch(IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

}
