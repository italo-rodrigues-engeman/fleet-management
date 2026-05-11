package com.indux.modules.request_budgets.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetDocumentoDTO {
    private String tipo;
    private String maisInformacoes;
    private String descricao;
    private List<String> anexos;
}

