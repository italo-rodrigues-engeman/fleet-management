package com.indux.modules.ppu.application.dtos.response.bm;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record BMGrid(
        String id,
        String contrato,
        PPUEntity ppu,
        String ppuApelido,
        DateRange periodo,
        String regional,
        DocumentStatus status,
        String criadoPor,
        Instant criadoEm,
        BigDecimal porcentagem
) {
}
