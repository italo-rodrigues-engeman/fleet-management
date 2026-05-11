package com.indux.modules.ppu.application.services.rdo.operation.audit;

public record AuditEquivalence(
        String id,
        String numberExpected,
        String numberActual,
        String descriptionExpected,
        String descriptionActual,
        String valueExpected,
        String valueActual
) {
}
