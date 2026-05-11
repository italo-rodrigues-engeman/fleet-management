package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ItemDetailedDTO {
    
    @JsonProperty("informacoes_basicas")
    private BasicInformation basicInformation;
    
    @JsonProperty("datas")
    private Dates dates;
    
    @JsonProperty("totais")
    private Totals totals;
    
    @JsonProperty("analise_precos")
    private PriceAnalysis priceAnalysis;
    
    @JsonProperty("detalhes_regionais")
    private List<RegionalDetailDTO> regionalDetails;
    
    // ==================== INNER CLASSES ====================
    
    @Data
    @NoArgsConstructor
    public static class BasicInformation {
        @JsonProperty("codigo_item")
        private Integer idItem;
        
        @JsonProperty("descricao_curta")
        private String itemName;
        
       
        
        @JsonProperty("codigo_grupo")
        private Integer groupCode;
        
        @JsonProperty("nome_grupo")
        private String groupName;
        
        @JsonProperty("usuario_cadastro")
        private String registerUser;
        
        @JsonProperty("usuario_edicao")
        private String registerUserEdition;

        @JsonProperty("Tipo_de_Item")
        private String itemType;


    }
    
    @Data
    @NoArgsConstructor
    public static class Dates {
        @JsonProperty("data_cadastro")
        private LocalDate creationDate;
        
        @JsonProperty("data_primeira_compra")
        private LocalDate firstPurchaseDate;
        
        @JsonProperty("data_ultima_compra")
        private LocalDate lastPurchaseDate;
		
		@JsonProperty("primeira_compra_dados")
		private PriceDetail firstPurchaseDetail;
		
		@JsonProperty("ultima_compra_dados")
		private PriceDetail lastPurchaseDetail;
    }
    
    @Data
    @NoArgsConstructor
    public static class Totals {
        @JsonProperty("quantidade_total_comprada")
        private BigDecimal totalqtdItensPurchased;
        
        @JsonProperty("valor_total_gasto")
        private BigDecimal totalValueSpent;
        
        @JsonIgnore
        @JsonProperty("preco_medio")
        private BigDecimal averagePrice;
        
        @JsonProperty("total_pedidos_geral")
        private BigDecimal totalOrdersGeneral;
    }
    
    @Data
    @NoArgsConstructor
    public static class PriceAnalysis {
        @JsonProperty("preco_minimo")
        private PriceDetail minimumPrice;
        
        @JsonProperty("preco_maximo")
        private PriceDetail maximumPrice;
        
        @JsonProperty("preco_medio") 
        private PriceDetail averagePrice;
    }
    
    @Data
    @NoArgsConstructor
    public static class PriceDetail {
        @JsonProperty("valor")
        private BigDecimal value;
        
        @JsonProperty("fornecedor")
        private String supplier;
        
		@JsonIgnore
        @JsonProperty("usuario_inclusao")
        private String inclusionUser;
        
		@JsonIgnore
        @JsonProperty("usuario_edicao")
        private String editionUser;
        
        @JsonProperty("data")
        private LocalDate date;
		
        @JsonProperty("solicitante")   
        private String requester;
        
        @JsonProperty("comprador") 
        private String buyer;
        
        @JsonProperty("filial") 
        private String branch;
        
        @JsonProperty("regional") 
        private String regional;

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