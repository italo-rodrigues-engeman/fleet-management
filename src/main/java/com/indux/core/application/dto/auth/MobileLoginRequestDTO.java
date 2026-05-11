package com.indux.core.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MobileLoginRequestDTO(
        @NotBlank(message = "CPF é obrigatório")
        String cpf,
        @NotNull(message = "Data de nascimento é obrigatória")
        LocalDate dataNascimento
) {
}
