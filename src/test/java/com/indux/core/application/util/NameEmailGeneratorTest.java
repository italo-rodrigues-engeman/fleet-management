package com.indux.core.application.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameEmailGeneratorTest {

    @Test
    @DisplayName("Should generate email from simple name")
    void shouldGenerateEmailFromSimpleName() {
        String email = NameEmailGenerator.generateEmail("Alan Imbeloni");
        assertEquals("alan.imbeloni@kogni.com.br", email);
    }

    @Test
    @DisplayName("Should remove accents from name")
    void shouldRemoveAccentsFromName() {
        String email = NameEmailGenerator.generateEmail("José María");
        assertEquals("jose.maria@kogni.com.br", email);
    }

    @Test
    @DisplayName("Should remove special characters")
    void shouldRemoveSpecialCharacters() {
        String email = NameEmailGenerator.generateEmail("João da Silva (Dev)");
        assertEquals("joao.da.silva.dev@kogni.com.br", email);
    }

    @Test
    @DisplayName("Should handle multiple spaces")
    void shouldHandleMultipleSpaces() {
        String email = NameEmailGenerator.generateEmail("Pedro   Antonio   Silva");
        assertEquals("pedro.antonio.silva@kogni.com.br", email);
    }

    @Test
    @DisplayName("Should handle empty name")
    void shouldHandleEmptyName() {
        assertNull(NameEmailGenerator.generateEmail(""));
        assertNull(NameEmailGenerator.generateEmail(null));
    }

    @Test
    @DisplayName("Should handle name with numbers")
    void shouldHandleNameWithNumbers() {
        String email = NameEmailGenerator.generateEmail("João Silva 2");
        assertEquals("joao.silva.2@kogni.com.br", email);
    }

    @Test
    @DisplayName("Should remove leading and trailing dots")
    void shouldRemoveLeadingAndTrailingDots() {
        String email = NameEmailGenerator.generateEmail("...João Silva...");
        assertEquals("joao.silva@kogni.com.br", email);
    }

    @Test
    @DisplayName("Should handle name with only special characters")
    void shouldHandleNameWithOnlySpecialCharacters() {
        assertNull(NameEmailGenerator.generateEmail("@#$%"));
    }
}
