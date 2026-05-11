package com.indux.modules.budgets.application.dto;

import com.indux.modules.budgets.domain.model.SimpleBudgetItem;
import lombok.Data;

import java.util.List;

@Data
public class UpdateBudgetItemsRequest {
    private List<SimpleBudgetItem> itens;
}
