package com.indux.modules.budgets.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddBudgetServicesRequest {
    private List<BudgetServiceItemDTO> servicos;
}
