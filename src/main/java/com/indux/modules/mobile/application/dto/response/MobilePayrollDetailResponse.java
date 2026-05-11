package com.indux.modules.mobile.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Date;
import java.util.List;

@Builder
public record MobilePayrollDetailResponse(
        @JsonProperty("competencia")
        Date competence,
        @JsonProperty("eventos")
        List<MobilePayrollEventResponse> events
) {
}
