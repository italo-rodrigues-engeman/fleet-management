package com.indux.modules.ppu.presentation.dtos.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuditResponse(
        String id,
        @JsonProperty("linha") String line,
        @JsonProperty("rdo") String rdo,
        @JsonProperty("samc") String samc,
        @JsonProperty("data") String data,
        @JsonProperty("texto") String text,
        @JsonProperty("linhaCheck") String lineCheck,
        @JsonProperty("plataforma") String platform
) {
}

