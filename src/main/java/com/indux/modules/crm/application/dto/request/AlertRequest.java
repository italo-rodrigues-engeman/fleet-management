package com.indux.modules.crm.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record AlertRequest(
        @JsonProperty("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @JsonProperty("engemanAgent") String engemanAgent,
        @JsonProperty("futureActionDescription") String futureActionDescription
) {}
