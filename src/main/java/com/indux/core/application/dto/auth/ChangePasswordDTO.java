package com.indux.core.application.dto.auth;

public record ChangePasswordDTO(String cpf, String oldPassword, String newPassword, String confirmPassword) {
}
