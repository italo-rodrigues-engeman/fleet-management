package com.indux.modules.ppu.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public record AvailablePeriod(
        @JsonProperty("matriculas") @NotNull List<String> registrations,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @JsonProperty("inicio") @NotNull LocalDate start,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @JsonProperty("fim") @NotNull LocalDate end,
        @JsonProperty("plataforma") @NotNull String platform,
        @JsonProperty("servico") @NotNull String serviceId,
        @NotNull String ppuId,
        @JsonProperty("tipo") @Nullable String type
        ) {
}
