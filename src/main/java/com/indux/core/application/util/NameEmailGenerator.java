package com.indux.core.application.util;

import java.text.Normalizer;

public class NameEmailGenerator {
    private static final String EMAIL_DOMAIN = "@kogni.com.br";

    public static String generateEmail(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }

        String normalized = fullName.toLowerCase().trim();
        normalized = removeAccents(normalized);
        normalized = normalized.replaceAll("\\s+", ".");
        normalized = normalized.replaceAll("[^a-z0-9.]", "");
        normalized = normalized.replaceAll("\\.{2,}", ".");
        normalized = normalized.replaceAll("^\\.|\\.$", "");

        if (normalized.isBlank()) {
            return null;
        }

        return normalized + EMAIL_DOMAIN;
    }

    private static String removeAccents(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }
}
