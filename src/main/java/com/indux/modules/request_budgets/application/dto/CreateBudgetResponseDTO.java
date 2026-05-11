package com.indux.modules.request_budgets.application.dto;

public record CreateBudgetResponseDTO(
    String message,
    int status,
    String budgetId,
    String nomeOportunidade
) {
    public static CreateBudgetResponseDTO success(String message, String budgetId, String nomeOportunidade) {
        return new CreateBudgetResponseDTO(message, 201, budgetId, nomeOportunidade);
    }
}

