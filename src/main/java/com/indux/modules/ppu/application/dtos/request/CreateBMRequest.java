package com.indux.modules.ppu.application.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.generic.DateRange;
import jakarta.validation.constraints.NotNull;

public record CreateBMRequest(
        @NotNull @JsonProperty("ppuId") String ppuId,
        @NotNull @JsonProperty("projeto") Long projectId,
        @NotNull @JsonProperty("periodo") DateRange period
) {
}
