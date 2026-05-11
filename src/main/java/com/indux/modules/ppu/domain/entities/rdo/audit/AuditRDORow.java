package com.indux.modules.ppu.domain.entities.rdo.audit;

import jakarta.annotation.Nullable;
import lombok.Builder;

@Builder
public record AuditRDORow(
        String id,
        String number,
        String auditName,
        String name,
        String quantity,
        @Nullable Boolean fullTime
) {
}
