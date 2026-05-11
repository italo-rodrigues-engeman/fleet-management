package com.indux.modules.budgets.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSimpleBudgetResponseDTO {
    private String message;
    private Integer status;
    private String budgetId;
    private String nomeOportunidade;
    private String budgetStatus;
}

