package com.indux.modules.ppu.application.dtos.response.competence;

import java.util.Set;

public record RDOConsolidationResponse(
        String matricula,
        String nome,
        String evento,
        String referencia,
        String valor,
        Set<String> plataformas,
        String competencia
) {
}
