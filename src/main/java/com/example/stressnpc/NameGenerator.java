package com.example.stressnpc;

import java.util.Locale;
import java.util.Random;
import java.util.Set;

public final class NameGenerator {

    private static final String[] PREFIXES = {"Vortex", "Ghost", "Cyber", "Dark", "Epic"};
    private static final String[] ROOTS = {"Gamer", "Craft", "Knight", "Hunter", "Wolf"};
    private static final String[] SUFFIXES = {"YT", "Pro", "_X", "777"};

    private static final Random RANDOM = new Random();

    private NameGenerator() {
    }

    public static String generate() {
        String name;
        do {
            String prefix = PREFIXES[RANDOM.nextInt(PREFIXES.length)];
            String root = ROOTS[RANDOM.nextInt(ROOTS.length)];
            String suffix = SUFFIXES[RANDOM.nextInt(SUFFIXES.length)];
            name = prefix + root + suffix;
        } while (isForbidden(name));
        return name;
    }

    public static String generateUnique(Set<String> usedNames) {
        String name;
        do {
            name = generate();
        } while (usedNames.contains(name.toLowerCase(Locale.ROOT)));
        return name;
    }

    private static boolean isForbidden(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("bot") || lower.matches("(?i)^bot[0-9_\\-]+.*$");
    }
}
