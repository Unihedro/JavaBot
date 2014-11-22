package com.gmail.inverseconduit.javadoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.datatype.ChatMessage;

public class JavaDocAccessor {
	private final ChatInterface chatInterface;
	private final JavadocDao dao;

	/**
	 * @param chatInterface interface to the chat
	 * @param dir the directory to the Javadocs folder
	 * @throws IOException if there's a problem reading a Javadoc file
	 */
	public JavaDocAccessor(ChatInterface chatInterface, Path dir) throws IOException {
		this.chatInterface = chatInterface;

		dao = new JavadocDao();

		Path java8Api = dir.resolve("java8.zip");
		if (Files.exists(java8Api)) {
			PageLoader loader = new ZipPageLoader(java8Api);
			PageParser parser = new Java8PageParser();
			dao.addJavadocApi(loader, parser);
		} else {
			//for testing purposes
			//this ZIP only hass the "java.lang.String" class
			Path sample = dir.resolve("sample.zip");
			if (Files.exists(sample)) {
				PageLoader loader = new ZipPageLoader(sample);
				PageParser parser = new Java8PageParser();
				dao.addJavadocApi(loader, parser);
			}
		}
	}

	public void javadoc(ChatMessage chatMessage, String commandText) {
		String response;
		try {
			response = generateResponse(commandText);
		} catch (IOException e) {
			throw new RuntimeException("Problem getting Javadoc info.", e);
		}

		response = "@" + chatMessage.getUsername() + " " + response;
		chatInterface.sendMessage(chatMessage.getSite(), chatMessage.getRoomId(), response);
	}

	private String generateResponse(String commandText) throws IOException {
		ClassInfo info;
		try {
			info = dao.getClassInfo(commandText);
		} catch (MultipleClassesFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Which one do you mean?");
			for (String name : e.getClasses()) {
				sb.append("\n* ").append(name);
			}
			return sb.toString();
		}

		if (info == null) {
			return "Sorry, I never heard of that class. :(";
		}

		//get the class description
		String description = info.getDescription();
		int pos = description.indexOf("\n");
		if (pos >= 0) {
			//just display the first paragraph
			description = description.substring(0, pos);
		}

		String url = info.getUrl();
		if (url == null) {
			return "**`" + info.getFullName() + "`**: " + description;
		}

		return "[**`" + info.getFullName() + "`**](" + url + " \"View the Javadocs\"): " + description;
	}
}
