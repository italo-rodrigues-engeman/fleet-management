package com.indux.modules.ppu.application.dtos.requests;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AvailableTypeRequest(
        @JsonProperty("nome") String name,
        @JsonProperty("fator") Double factor,
        @JsonProperty("quantidadeDias") int quantityDays
) {
}
