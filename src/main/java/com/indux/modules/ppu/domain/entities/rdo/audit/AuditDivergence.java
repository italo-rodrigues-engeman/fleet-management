package com.indux.modules.ppu.domain.entities.rdo.audit;

public record AuditDivergence(
        String id,
        String line,
        String rdo,
        String samc,
        String data,
        String text,
        String lineCheck,
        String platform
) {
}
