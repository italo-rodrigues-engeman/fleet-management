package com.indux.modules.ppu.application.dtos.item;

import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.ppu.AuditableLineConfig;
import com.mongodb.lang.Nullable;

import java.util.List;

public record LineDTO(
        @Nullable String id,
        String numero,
        String numeroPPU,
        String nome,
        String unidadeMedida,
        Double valor,
        Double fator,
        List<MeasurementForecast> totalPrevisto,
        @Nullable List<String> plataformas,
        @Nullable String linhaAuditavel,
        @Nullable List<AuditableLineConfig> linhasAuditaveis
) {
}
