package com.indux.modules.ppu.application.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request para justificativa em lote de divergências MIO.
 * Permite justificar múltiplas divergências em uma única requisição.
 */
public record MioDivergenceJustificationRequest(
        @NotEmpty @Valid List<MioDivergenceJustificationItem> items) {
}
