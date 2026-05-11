package com.indux.modules.ocf.application.dto;

import java.util.List;

public record CreateOccurrencesBatchResponseDTO(
        String message,
        int status,
        int totalCriadas,
        int totalErros,
        List<OccurrenceCreatedInfo> ocorrenciasCriadas,
        List<ErroCriacao> erros
) {
    public record OccurrenceCreatedInfo(
            String matricula,
            String occurrenceId,
            Long codeID
    ) {}
    
    public record ErroCriacao(
            String matricula,
            String motivo
    ) {}
}


