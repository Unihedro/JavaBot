package com.gmail.inverseconduit.utils;

public class PrintUtils {

    public static String FixedFont(String msg) {
        if (msg.isEmpty())
            return msg;
        StringBuilder builder = new StringBuilder();
        for (String segment : msg.split("\n"))
            builder.append("    ").append(segment);
        return builder.toString();
    }
}
