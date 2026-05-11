package com.indux.core.application.dto.auth;

public record MobileLoginResponseDTO(
        boolean success,
        String message,
        String token
) {
    public static MobileLoginResponseDTO success(String token) {
        return new MobileLoginResponseDTO(true, "Login mobile realizado com sucesso", token);
    }

    public static MobileLoginResponseDTO failure(String message) {
        return new MobileLoginResponseDTO(false, message, null);
    }
}
