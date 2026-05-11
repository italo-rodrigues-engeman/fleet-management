package com.indux.modules.budgets.application.dto;

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
public class SimpleBudgetSummaryDTO {
    private String id;
    private String nomeOportunidade;
    private Long clienteId;
    private String clienteNome;
    private Integer oportunidade;
    private String status;
    private Integer step;
    private String orcamentista;
    private java.util.Date dataCriacao;
}
