package com.indux.modules.mobile.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Date;

@Builder
public record MobilePayrollEventResponse(
        @JsonProperty("id")
        String id,
        @JsonProperty("dataPagamento")
        Date paymentDate,
        @JsonProperty("nomeEvento")
        String eventName,
        @JsonProperty("descEvento")
        String eventDescription,
        @JsonProperty("valor")
        Double value
) {
}
