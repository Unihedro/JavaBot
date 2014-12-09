package com.gmail.inverseconduit.bot.interactions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Interactions {

    private static final Set<Interaction> permInteractions = new HashSet<>();

    static {
        permInteractions.add(howDoI());
        permInteractions.add(thatWord());
    }

    public static Set<Interaction> getPerminteractions() {
        return Collections.unmodifiableSet(permInteractions);
    }

    private static Interaction howDoI() {
        return new Interaction((s) -> {
            return s.startsWith("how do I") || s.startsWith("how do i");
        }, "~ Write **code**");
    }

    private static Interaction thatWord() {
        return new Interaction((s) -> s.contains("you keep using that word"), "https://www.youtube.com/watch?v=G2y8Sx4B2Sk");
    }
}
