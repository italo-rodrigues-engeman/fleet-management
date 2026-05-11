package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ItemGroupedDTO {
    
    // ==================== CAMPOS VISÍVEIS NO JSON ====================

    @JsonProperty("codigo_item")
    private Integer idItem;
    
    @JsonProperty("nome_item")
    private String itemName;
    
    @JsonProperty("codigo_grupo")
    private Integer groupCode;

    @JsonProperty("nome_grupo")
    private String groupName;
    
   

    @JsonProperty("status_do_item") 
    private String status;

    @JsonProperty("data_cadastro") 
    private LocalDate creationDate;
    
    @JsonProperty("total_comprado")
    private BigDecimal totalPurchased;
    
    @JsonProperty("preco_medio")
    private BigDecimal averagePrice;

    // ==================== CAMPOS OCULTOS (IGNORE) ====================

    @JsonProperty("quantidade_pedidos")
    private Long orderCount; // Corrigido de supplierCount para orderCount conforme a Query

    @JsonProperty("Unidade_de_Medida")
    private String unitOfMeasure;
    
    @JsonProperty("Tipo_Item")
    private String itemType;
    

    
    public ItemGroupedDTO(Integer idItem, 
                          String itemName, 
                          Integer groupCode,
                          String groupName,
                        
                          String status,
                          LocalDate creationDate,                                   
                          Number totalQuantity, 
                          Number averagePrice, 
                          Number orderCount,
                          String unitOfMeasure,
                          String itemType) {
        
        this.idItem = idItem;
        this.itemName = itemName;
        this.groupCode = groupCode;
        this.groupName = groupName;
        
        this.status = status;
        this.creationDate = creationDate;

        // Tratamento de Total Comprado
        this.totalPurchased = (totalQuantity != null) 
                ? new BigDecimal(totalQuantity.toString()) 
                : BigDecimal.ZERO;
        
        // Tratamento de Preço Médio com Arredondamento
        if (averagePrice != null) {
            this.averagePrice = new BigDecimal(averagePrice.toString())
                    .setScale(2, RoundingMode.HALF_UP); 
        } else {
            this.averagePrice = BigDecimal.ZERO;
        }
        
        // Tratamento do Contador
        this.orderCount = (orderCount != null) ? orderCount.longValue() : 0L;

        this.unitOfMeasure = unitOfMeasure;
        this.itemType = itemType;
    }
}