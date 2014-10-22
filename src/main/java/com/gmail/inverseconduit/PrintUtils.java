package com.gmail.inverseconduit;

import java.util.regex.Pattern;

public class PrintUtils {
    public static String FixedFont(String msg) {
        StringBuilder stringBuilder = new StringBuilder();
        for(String m : msg.split("\\n")) {
            stringBuilder.append("    ").append(m);
        }
        return stringBuilder.toString();
    }
}
