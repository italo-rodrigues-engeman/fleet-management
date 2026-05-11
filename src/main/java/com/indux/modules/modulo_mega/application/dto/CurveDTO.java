package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;


@Data
@NoArgsConstructor
public class CurveDTO {

    // ==================== DADOS BÁSICOS ====================
        
    @JsonProperty("codigo_item")
    private Integer idItem; 

    @JsonProperty("descricao_curta")
    private String itemName;
   
    @JsonProperty("codigo_grupo")
    private Integer groupCode;

    @JsonProperty("nome_grupo")
    private String groupName;

    @JsonProperty("status")
    private String orderStatus;
        
    @JsonProperty("curva_classificacao")
    private String curveClassification;

    // ==================== AGREGADOS (Valores Matemáticos) ====================

    @JsonProperty("qtd_total_comprado_grupo")
    private BigDecimal totalGroupqtdItens;
    
    @JsonProperty("qtd_total_item")
    private BigDecimal totalItemqtdItens;

    @JsonProperty("valor_total_comprado_grupo") 
    private BigDecimal totalGroupValue; 

    @JsonProperty("preco_medio_unitario_item")
    private BigDecimal averageUnitPrice; 
    
    @JsonProperty("total_pedidos_geral_item") 
    private Integer totalOrdersCount; 
}