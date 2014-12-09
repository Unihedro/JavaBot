package com.gmail.inverseconduit.bot.interactions;

import java.util.function.Predicate;

public class Interaction {

    private final Predicate<String> condition;

    private final String            response;

    public Interaction(Predicate<String> condition, String response) {
        this.condition = condition;
        this.response = response;
    }

    public Predicate<String> getCondition() {
        return condition;
    }

    public String getResponse() {
        return response;
    }

}
