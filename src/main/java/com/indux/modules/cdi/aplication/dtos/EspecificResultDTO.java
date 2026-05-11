package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

public record EspecificResultDTO(
        @JsonProperty("trabalho") @Nullable String labor,
        @JsonProperty("produtividade") @Nullable String productivity,
        @JsonProperty("producao") @Nullable String production,
        @JsonProperty("qualidade") @Nullable String quality,
        @JsonProperty("seguranca") @Nullable String security,
        @JsonProperty("custo") @Nullable String cost,
        @JsonProperty("perdas") @Nullable String loses,
        @JsonProperty("produtivo") @Nullable String productive,
        @JsonProperty("rastreabilidade") @Nullable String traceability,
        @JsonProperty("certificacao") @Nullable String certification,
        @JsonProperty("outros") @Nullable String outhers
) {
}
