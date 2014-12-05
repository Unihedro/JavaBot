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

    public static String truncate(String message) {
        if (message.lastIndexOf(".") < 500 && message.lastIndexOf(".") != -1) { return message.substring(0, message.lastIndexOf(".")); }
        message = message.substring(0, 495);
        //this one can cut quite a lot of stuff..
        if (message.lastIndexOf(".") < 500 && message.lastIndexOf(".") != -1) { return message.substring(0, message.lastIndexOf(".")); }
        return message + "...";
    }
}
