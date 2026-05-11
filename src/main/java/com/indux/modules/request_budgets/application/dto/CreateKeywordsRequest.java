package com.indux.modules.request_budgets.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateKeywordsRequest {
    @NotNull(message = "keywords não pode ser nulo")
    private List<String> keywords;
}


