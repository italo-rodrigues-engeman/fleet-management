package com.indux.core.application.service.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationValidatorTest {

    @Test
    @DisplayName("Should pass when password is valid and strong")
    void shouldPassValidPassword() {
        List<String> failures = AuthenticationValidator.validatePassword("StrongPass@123");
        assertTrue(failures.isEmpty(), "Password should have no validation failures");
    }

    @Test
    @DisplayName("Should fail when password is too short")
    void shouldFailShortPassword() {
        List<String> failures = AuthenticationValidator.validatePassword("Str@12");
        assertFalse(failures.isEmpty());
        assertTrue(failures.contains("A senha deve possuir pelo menos 08 caracteres."));
    }

    @Test
    @DisplayName("Should fail when password has no uppercase letter")
    void shouldFailNoUppercase() {
        List<String> failures = AuthenticationValidator.validatePassword("weakpass@123");
        assertTrue(failures.contains("A senha deve possuir pelo menos uma letra maiúscula."));
    }

    @Test
    @DisplayName("Should fail when password has no lowercase letter")
    void shouldFailNoLowercase() {
        List<String> failures = AuthenticationValidator.validatePassword("WEAKPASS@123");
        assertTrue(failures.contains("A senha deve possuir pelo menos uma letra minúscula."));
    }

    @Test
    @DisplayName("Should fail when password has no numbers")
    void shouldFailNoNumbers() {
        List<String> failures = AuthenticationValidator.validatePassword("StrongPass@!");
        assertTrue(failures.contains("A senha deve possuir pelo menos um numero."));
    }

    @Test
    @DisplayName("Should fail when password has no special characters")
    void shouldFailNoSpecialChars() {
        List<String> failures = AuthenticationValidator.validatePassword("StrongPass123");
        assertTrue(failures.contains("A senha deve possuir pelo menos um caractere especial."));
    }

    @Test
    @DisplayName("Should sanitize CPF correctly keeping only digits")
    void shouldSanitizeCpf() {
        assertEquals("12345678910", AuthenticationValidator.sanitizeCpf("123.456.789-10"));
        assertEquals("00011122233", AuthenticationValidator.sanitizeCpf("000.111.222-33"));
    }

    @Test
    @DisplayName("Should return 0 when phone number is blank or null")
    void shouldReturnZeroForBlankPhone() {
        assertEquals(0L, AuthenticationValidator.normalizePhoneNumber(""));
        assertEquals(0L, AuthenticationValidator.normalizePhoneNumber(null));
        assertEquals(0L, AuthenticationValidator.normalizePhoneNumber("   "));
    }

    @Test
    @DisplayName("Should normalize valid phone number")
    void shouldNormalizeValidPhoneNumber() {
        assertEquals(81999999999L, AuthenticationValidator.normalizePhoneNumber("(81) 99999-9999"));
        assertEquals(11987654321L, AuthenticationValidator.normalizePhoneNumber("11 98765 4321"));
    }

    @Test
    @DisplayName("Should return 0 for phone number with invalid length after sanitization")
    void shouldReturnZeroForInvalidLengthPhone() {
        assertEquals(0L, AuthenticationValidator.normalizePhoneNumber("(81) 9999-9999"));

        assertEquals(0L, AuthenticationValidator.normalizePhoneNumber("8199999999999"));
    }
}
