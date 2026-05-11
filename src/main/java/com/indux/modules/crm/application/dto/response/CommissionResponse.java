package com.indux.modules.crm.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommissionResponse(
        @JsonProperty("id") String id,
        @JsonProperty("valor_inicial") Double initialValue,
        @JsonProperty("valor_final") Double finalValue,
        @JsonProperty("porcentagem") Float percentage
) {
}
