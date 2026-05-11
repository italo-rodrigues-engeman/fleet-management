package com.indux.core.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleLoginRequestDTO {
    
    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;
    
    @NotBlank(message = "CPF é obrigatório")
    private String cpf;
    
    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate dataNascimento;
}
