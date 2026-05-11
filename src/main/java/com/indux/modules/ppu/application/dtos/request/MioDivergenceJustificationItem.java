package com.indux.modules.ppu.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Item de justificativa para uma divergência MIO específica.
 * A combinação de {@code registration} + {@code date} identifica unicamente a
 * divergência.
 */
public record MioDivergenceJustificationItem(
        @NotBlank String registration,
        @NotNull LocalDate date,
        @NotBlank String justification) {
}
