package com.indux.modules.budgets.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleBudgetItem {
    private String nome;
    private String descricao;
    private String grupo;
    private String grupoItem;
    private Integer codGrupo;
    private String fase;
    @JsonProperty("unidade_medida")
    private String unidadeMedida;
    @JsonProperty("tipo_item")
    private String tipoItem;
    private Integer codMega;
    private BigDecimal valorConsiderado;
    private Double quantidade;
    private BigDecimal valorOrcamento;
    @JsonProperty("preco_medio")
    private BigDecimal precoMedio;
}
