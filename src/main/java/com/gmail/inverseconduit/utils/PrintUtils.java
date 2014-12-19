package com.gmail.inverseconduit.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrintUtils {

    private static final String  linkTokenRegex          = "(\\[[^]]++\\]\\(https?+:\\/\\/[^\\s\"]++(\\h++\"[^\"]++\")?\\))";

    private static final String  tagTokenRegex           = "(\\[(meta)?+tag:[^\\]]++\\])";

    private static final String  strikethroughTokenRegex = "(---.*?---)";

    private static final String  codeTokenRegex          = "(\\`[^\\`]++\\`)";

    private static final String  markdownTokenRegex      = "([*_]{1,3}.*?[*_]{1,3})";

    private static final String  wordTokenRegex          = "([^\\s]++)";

    private static final String  messageTokenRegex       = "(" + linkTokenRegex + "|" + tagTokenRegex + "|" + markdownTokenRegex + "|" + strikethroughTokenRegex + "|"
                                                             + codeTokenRegex + "|" + wordTokenRegex + ")*";

    private static final Pattern markdownTokenizer       = Pattern.compile(messageTokenRegex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);

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
        message = message.substring(0, 495);
        //this one can cut quite a lot of stuff..
        if (message.lastIndexOf(".") < 500 && message.lastIndexOf(".") != -1) { return message.substring(0, message.lastIndexOf(".")); }
        return message + "...";
    }

    public static List<String> splitUsefully(String message) {
        Matcher m = markdownTokenizer.matcher(message);
        List<String> tokens = new ArrayList<>();
        while (m.find()) {
            String match = m.group(0);
            if (match != null && !match.trim().isEmpty()) {
                tokens.add(match);
            }
        }
        return tokens;
    }
}
