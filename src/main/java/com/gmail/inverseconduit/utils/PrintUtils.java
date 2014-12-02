package com.gmail.inverseconduit.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrintUtils {

    private static final String  messageTokenRegex =
                                                           "(\\[[^]]++\\]\\(https?+:\\/\\/[^\\s\"]++\\h*\\\"[^\"]++\\\"\\)|([-*_]{1,3})?+\\[(meta\\-)?tag:[^]]++\\]\\2|\\`.*?\\`|[\\w\\.]*)";

    private static final Pattern markdownTokenizer = Pattern.compile(messageTokenRegex);

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
        m.find();
        int groups = m.groupCount();
        List<String> tokens = new ArrayList<>();
        for (int i = 1; i <= groups; i++ ) {
            tokens.add(m.group(i));
        }
        return tokens;
    }
}
