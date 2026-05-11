package com.indux.modules.ppu.application.dtos.response.competence;

import com.mongodb.lang.Nullable;

import java.util.Set;

public record ConsolidationRecord(
        String matricula,
        String evento,
        String referencia,
        String valor,
        String nome,
        Set<String> plataformas,
        @Nullable String competencia
) {
}
