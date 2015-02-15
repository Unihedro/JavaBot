package com.gmail.inverseconduit.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.datatype.ChatMessage;

public class PrintUtils {

    private static final String  LINK_TOKEN_REGEX          = "(\\[[^]]++\\]\\(https?+:\\/\\/[^\\s\"]++(\\h++\"[^\"]++\")?\\))";

    private static final String  TAG_TOKEN_REGEX           = "(\\[(meta)?+tag:[^\\]]++\\])";

    private static final String  STRIKETHROUGH_TOKEN_REGEX = "(---.*?---)";

    private static final String  CODE_TOKEN_REGEX          = "(\\`[^\\`]++\\`)";

    private static final String  MARKDOWN_TOKEN_REGEX      = "([*_]{1,3}.*?[*_]{1,3})";

    private static final String  WORD_TOKEN_REGEX          = "([^\\s]++)";

    private static final String  MESSAGE_TOKEN_REGEX       = "(" + LINK_TOKEN_REGEX + "|" + TAG_TOKEN_REGEX + "|" + MARKDOWN_TOKEN_REGEX + "|" + STRIKETHROUGH_TOKEN_REGEX + "|"
                                                               + CODE_TOKEN_REGEX + "|" + WORD_TOKEN_REGEX + ")*";

    private static final Pattern MARKDOWN_TOKENIZER        = Pattern.compile(MESSAGE_TOKEN_REGEX, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);

    public static String fixedFont(String msg) {
        if (msg.isEmpty())
            return msg;
        StringBuilder builder = new StringBuilder();
        for (String segment : msg.split("\n"))
            builder.append("    ").append(segment);
        return builder.toString();
    }

    public static List<String> splitUsefully(String message) {
        Matcher m = MARKDOWN_TOKENIZER.matcher(message);
        List<String> tokens = new ArrayList<>();
        while (m.find()) {
            String match = m.group(0);
            if (match != null && !match.trim().isEmpty()) {
                tokens.add(match);
            }
        }
        return tokens;
    }

    public static String asReply(String result, ChatMessage chatMessage) {
        if (result.isEmpty()) { return ""; } // no meaning in replying with an empty message
        return String.format(":%d %s", chatMessage.getMessageId(), result);
    }
}
