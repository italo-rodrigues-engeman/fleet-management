package com.indux.modules.ocf.application.dto;

import java.util.Optional;

public record UpdateOccurrenceDTO(
        // Campos que podem ser atualizados
        Optional<String> tipoFluxo,
        Optional<String> competencia,
        Optional<String> prioridade,
        Optional<String> atendenteRHMatriz,
        Optional<String> emailAtendenteRHMatriz
) { } 