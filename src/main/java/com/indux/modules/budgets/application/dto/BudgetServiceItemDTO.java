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
public class BudgetServiceItemDTO {
    private String tipo;
    private String nome;
    private String descricao;
    private String fase;
    private String grupoItem;
    private List<BudgetServiceSupplierDTO> fornecedores;
}
