package com.indux.modules.ocf.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOccurrencesBatchDTO(
        @NotEmpty(message = "A lista de matrículas não pode estar vazia")
        List<String> matriculas,
        
        @NotNull(message = "A justificativa é obrigatória")
        String justificativaOcorrencia,
        
        @NotNull(message = "O tipo de atendimento é obrigatório")
        String tipoAtendimento,
        
        @NotNull(message = "A competência é obrigatória")
        String competencia
) {}


