package com.indux.modules.budgets.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleBudgetServiceItem {
    private String tipo;
    private String nome;
    private String descricao;
    private String fase;
    private String grupoItem;
    @Builder.Default
    private List<SimpleBudgetServiceSupplier> fornecedores = new ArrayList<>();
}
