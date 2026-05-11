package com.indux.modules.request_budgets.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoringEntryDTO {
    @NotNull
    private String value;

    @NotNull
    private Integer score;
}


