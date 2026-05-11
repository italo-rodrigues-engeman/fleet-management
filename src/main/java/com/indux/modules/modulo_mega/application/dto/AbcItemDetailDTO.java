package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbcItemDetailDTO {

    // ==================== IDENTIFICAÇÃO ====================
    
    @JsonProperty("codigo_grupo")
    private Integer groupCode; // Ex: "GRP-001"

    @JsonProperty("nome_grupo")
    private String groupName; // Ex: "Grupo Materiais A"

    @JsonProperty("codigo_item")
    private Integer idItem;  // Ex: "ITEM-001"

    @JsonProperty("nome_item")
    private String itemName;  // Ex: "Item A1"

    @JsonProperty("tipo_item")
    private String itemType;

    // ==================== MÉTRICAS (USADAS NO CÁLCULO) ====================

    // "Qtde Comprados: 150"
    @JsonProperty("qtde_comprada")
    private BigDecimal qtdItensTotal;

    @JsonProperty("valor_total_item")
    private BigDecimal totalValue;

     @JsonProperty("total_pedidos")
    private Long totalOrders; 

      // "Status: Ativo"
    @JsonProperty("status")
    private String status;

    

    // "R$ Valor Médio: R$ 100,00"
    @JsonProperty("preco_medio_unitario")
    private BigDecimal averagePrice;

    @JsonProperty("situacao")
    private String situation;

    @JsonProperty("tipo")
    private String orderType;

      
   

    @JsonIgnore
    @JsonProperty("codigo_projeto")
    private Integer projectCode;

    @JsonIgnore
    @JsonProperty("codigo_contrato")
    private Integer contractCode;

    @JsonIgnore
    @JsonProperty("contrato_nome")
    private String contractName;

    @JsonIgnore
    @JsonProperty("setor_id")
    private Integer sectorId; 

    @JsonIgnore
    @JsonProperty("setor_nome")
    private String sectorName;

    @JsonIgnore
    @JsonProperty("sigla_setor")
    private String acronymSector;

    @JsonIgnore
    @JsonProperty("regional_codigo")
    private Integer regionalCode;

    @JsonIgnore
    @JsonProperty("regional_nome")
    private String regionalName;

    @JsonIgnore
    @JsonProperty("sigla_regional")
    private String acronymRegional;

    @JsonIgnore
    @JsonProperty("superintendencia_id")
    private Integer superId;

    @JsonIgnore
    @JsonProperty("superintendencia_nome")
    private String superName;

    @JsonIgnore
    @JsonProperty("sigla_superintendencia")
    private String acronymSuper;

    @JsonIgnore
    @JsonProperty("diretoria_id")
    private Integer directoryId;

    @JsonIgnore
    @JsonProperty("diretoria_nome")
    private String directoryName;

    @JsonIgnore
    @JsonProperty("sigla_diretoria")
    private String acronymDirectory;

    

    

    // ==================== METADADOS ====================

   

    // Opcional: Campo para debug ou exibição visual (se a linha precisar de cor específica)
    // O Service irá preencher isso ("A", "B", ou "C")
    @JsonProperty("classificacao_calculada")
    private String calculatedCurveClass;
}