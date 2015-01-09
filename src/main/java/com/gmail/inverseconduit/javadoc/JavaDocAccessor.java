package com.gmail.inverseconduit.javadoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.gmail.inverseconduit.datatype.ChatMessage;

public class JavaDocAccessor {


    private final JavadocDao    dao;

    /**
     * @param chatInterface
     *        interface to the chat
     * @param dir
     *        the directory to the Javadocs folder
     * @throws IOException
     *         if there's a problem reading a Javadoc file
     */
    public JavaDocAccessor(Path dir) throws IOException {
        dao = new JavadocDao();

        Path java8Api = dir.resolve("java8.zip");
        if (Files.exists(java8Api)) {
            PageLoader loader = new ZipPageLoader(java8Api);
            PageParser parser = new Java8PageParser();
            dao.addJavadocApi(loader, parser);
        }
        else {
            //for testing purposes
            //this ZIP only has the "java.lang.String" class
            Path sample = dir.resolve("sample.zip");
            if (Files.exists(sample)) {
                PageLoader loader = new ZipPageLoader(sample);
                PageParser parser = new Java8PageParser();
                dao.addJavadocApi(loader, parser);
            }
        }
    }

    public String javadoc(ChatMessage chatMessage, String commandText) {
        String response;
        try {
            response = generateResponse(commandText);
        } catch(IOException e) {
            throw new RuntimeException("Problem getting Javadoc info.", e);
        }

        return ":" + chatMessage.getMessageId() + " " + response;
    }

    private String generateResponse(String commandText) throws IOException {
        ClassInfo info;
        try {
            info = dao.getClassInfo(commandText);
        } catch(MultipleClassesFoundException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Which one do you mean?");
            for (String name : e.getClasses()) {
                sb.append("\n* ").append(name);
            }
            return sb.toString();
        }

        if (info == null) { return "Sorry, I never heard of that class. :("; }

        StringBuilder sb = new StringBuilder();

        boolean deprecated = info.isDeprecated();
        for (String modifier : info.getModifiers()) {
            boolean italic = false;
            switch (modifier) {
            case "abstract":
            case "final":
                italic = true;
                break;
            case "class":
            case "enum":
            case "interface":
                italic = false;
                break;
            case "@interface":
                italic = false;
                modifier = "annotation";
                break;
            default:
                //ignore all the rest
                continue;
            }

            if (italic)
                sb.append('*');
            if (deprecated)
                sb.append("---");
            sb.append("[tag:").append(modifier).append("]");
            if (deprecated)
                sb.append("---");
            if (italic)
                sb.append('*');
            sb.append(' ');
        }

        if (deprecated)
            sb.append("---");
        String fullName = info.getFullName();
        String url = info.getUrl();
        if (url == null) {
            sb.append("**`").append(fullName).append("`**");
        }
        else {
            sb.append("[**`").append(fullName).append("`**](").append(url).append(" \"View the Javadocs\")");
        }
        if (deprecated)
            sb.append("---");
        sb.append(": ");

        //get the class description
        String description = info.getDescription();
        int pos = description.indexOf("\n");
        if (pos >= 0) {
            //just display the first paragraph
            description = description.substring(0, pos);
        }
        sb.append(description);

        return sb.toString();
    }
}
