package com.indux.core.domain.model.generic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter @AllArgsConstructor @NoArgsConstructor
public class DateRange {
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("inicio")
    private LocalDate start;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd") @JsonProperty("final") private LocalDate end;
}
