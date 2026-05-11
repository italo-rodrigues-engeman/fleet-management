package com.indux.core.application.service.auth;

import io.micrometer.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AuthenticationValidator {
    public static List<String> validatePassword(String pass) {
        List<String> failures = new ArrayList<>();

        validateLength(pass, failures);
        validateUppercase(pass, failures);
        validateLowercase(pass, failures);
        validateNumber(pass, failures);
        validateSpecialChars(pass, failures);

        return failures;
    }

    private static void validateLength(String pass, List<String> failures) {
        if (StringUtils.isBlank(pass) || pass.length() < 8) {
            failures.add("A senha deve possuir pelo menos 08 caracteres.");
        }
    }

    private static void validateUppercase(String pass, List<String> failures) {
        if (!Pattern.matches(".*[A-Z].*", pass)) {
            failures.add("A senha deve possuir pelo menos uma letra maiúscula.");
        }
    }

    private static void validateLowercase(String pass, List<String> failures) {
        if (!Pattern.matches(".*[a-z].*", pass)) {
            failures.add("A senha deve possuir pelo menos uma letra minúscula.");
        }
    }

    private static void validateNumber(String pass, List<String> failures) {
        if (!Pattern.matches(".*[0-9].*", pass)) {
            failures.add("A senha deve possuir pelo menos um numero.");
        }
    }

    private static void validatePhoneNumber(String phone, List<String> failures) {
        if (StringUtils.isBlank(phone)) {
            failures.add("O número de telefone não pode estar em branco.");
            return;
        }

        if (!phone.matches("^\\d+$")) {
            failures.add("O número de telefone deve conter apenas dígitos (DDD + número com 9 dígitos).");
        }
    }

    private static void validateSpecialChars(String pass, List<String> failures) {
        if (!Pattern.matches(".*[\\W].*", pass)) {
            failures.add("A senha deve possuir pelo menos um caractere especial.");
        }
    }

    public static String sanitizeCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    private boolean corporateEmail(String email) {
        return !email.contains("gmail") && !email.contains("outlook") && !email.contains("hotmail") && !email.contains("yahoo");
    }

    public static Long normalizePhoneNumber(String rawPhone) {
        if (StringUtils.isBlank(rawPhone)) {
            return 0L;
        }

        String digitsOnly = rawPhone.replaceAll("\\D", "");

        if (!digitsOnly.matches("^\\d{11}$")) {
            return 0L;
        }

        try {
            return Long.parseLong(digitsOnly);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

}
