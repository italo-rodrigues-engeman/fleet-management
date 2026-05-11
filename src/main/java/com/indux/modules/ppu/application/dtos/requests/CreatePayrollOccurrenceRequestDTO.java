package com.indux.modules.ppu.application.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreatePayrollOccurrenceRequestDTO(
        @NotEmpty(message = "A lista de matrículas não pode estar vazia")
        List<String> matriculas,
        
        @NotBlank(message = "A justificativa é obrigatória")
        String justificativaOcorrencia,
        
        @NotBlank(message = "O tipo de atendimento é obrigatório")
        String tipoAtendimento,
        
        @NotBlank(message = "A competência é obrigatória")
        String competencia
) {}
