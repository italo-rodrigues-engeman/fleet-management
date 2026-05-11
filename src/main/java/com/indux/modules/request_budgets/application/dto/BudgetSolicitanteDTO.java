package com.indux.modules.request_budgets.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetSolicitanteDTO {
    private String nome;
    private String funcaoCargo;
    private String telefone1;
    private String telefone2;
    private String email1;
    private String email2;
    private String localizacao;
    private String observacao;
}

