package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Getter
public abstract class BaseStatisticsDTO {

    // Cria o objeto "totais" no JSON
    @JsonProperty("totais")
    private GroupTotals totals;

    // Cria o objeto "analise_precos" no JSON
    @JsonProperty("analise_precos")
    private GroupPriceAnalysis priceAnalysis;

    // ==================== CLASSES INTERNAS (BUILDING BLOCKS) ====================

    @Data
    @NoArgsConstructor
    public static class GroupTotals {
        @JsonProperty("qtd_total_comprada")
        private BigDecimal totalqtdItens;

        @JsonProperty("valor_total_comprado")
        private BigDecimal totalValue;

        @JsonProperty("soma_pedidos")
        private BigDecimal totalOrders;
    }

    @Data
    @NoArgsConstructor
    public static class GroupPriceAnalysis {
        @JsonProperty("preco_minimo")
        private GroupPriceDetail minimumPrice;

        @JsonProperty("preco_maximo")
        private GroupPriceDetail maximumPrice;
        
        @JsonProperty("preco_medio") 
        private GroupPriceDetail averagePrice;
    }

    @Data
    @NoArgsConstructor
    public static class GroupPriceDetail {
        @JsonProperty("valor")
        private BigDecimal value;
		
		@JsonProperty("solicitante")
        private String requester; 
		
		@JsonProperty("regional")
        private String regional;
		
		@JsonProperty("data")
        private LocalDate date;
		
		@JsonProperty("comprador")
        private String buyer;

        @JsonProperty("fornecedor")
        private String supplier;
              

        @JsonProperty("usuario_inclusao")
		@JsonIgnore		
        private String inclusionUser;

        @JsonProperty("usuario_alteracao")
		@JsonIgnore
        private String alterationUser; 
    
        @JsonProperty("filial")
		private String branch;

        @JsonProperty("unidade_de_medida") 
        private String unitOfMeasure;

        @JsonProperty("Tipo de Compra")
		private String orderType;

        @JsonProperty("solicitacao") 
        private Integer solicitation;

        @JsonProperty("ap") 
        private String approve;

        @JsonProperty("pedido") 
        private Integer orderNumber;
        
        
    }
}