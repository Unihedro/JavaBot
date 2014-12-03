package com.gmail.inverseconduit.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrintUtils {

    private static final String  messageTokenRegex =
                                                           "(((\\[[^]]++\\]\\(https?+:\\/\\/[^\\s\"]++(\\s++\\\"[^\"]++\\\")?\\)[^\\s]++)|([-*_]{1,3})?+\\[(meta-)?tag:[^\\]]++\\]\\2|([-\\`_*]{1,3})?.*?\\2|[^\\s]++)*)";

    private static final Pattern markdownTokenizer = Pattern.compile(messageTokenRegex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);

    public static String FixedFont(String msg) {
        if (msg.isEmpty())
            return msg;
        StringBuilder builder = new StringBuilder();
        for (String segment : msg.split("\n"))
            builder.append("    ").append(segment);
        return builder.toString();
    }

    public static String truncate(String message) {
        if (message.lastIndexOf(".") < 500 && message.lastIndexOf(".") != -1) { return message.substring(0, message.lastIndexOf(".")); }
        message = message.substring(0, 499);
        if (message.lastIndexOf(".") < 500 && message.lastIndexOf(".") != -1) { return message.substring(0, message.lastIndexOf(".")); }
        return message;
    }

    public static List<String> splitUsefully(String message) {
        Matcher m = markdownTokenizer.matcher(message);
        List<String> tokens = new ArrayList<>();
        while (m.find()) {
            // Matcher groups are "incorrect", since the matchers don't respect 
            // word-boundaries and split stuff into large sentence blocks. 
            // This is undesirable, but for now it works... 
            // TODO: Fix regex to properly subdivide non-(link|tag|markdown) stuff
            String match = m.group(0);
            if (match != null && !match.trim().isEmpty()) {
                tokens.add(match);
            }
        }
        return tokens;
    }
}
