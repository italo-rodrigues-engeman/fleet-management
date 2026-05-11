package com.indux.modules.mobile.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Date;

@Builder
public record MobilePayrollSummaryResponse(
        @JsonProperty("competencia")
        Date competence
) {
}
