package com.indux.modules.ppu.application.dtos.response;

import com.indux.modules.ppu.domain.entities.bm.RMLog;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record RMLogResponse(
        List<RMLog> rms,
        BigDecimal totalRM,
        BigDecimal totalBM,
        Double porcentagem
) {
}
