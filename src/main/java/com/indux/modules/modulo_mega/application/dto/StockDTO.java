package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class StockDTO {

    @JsonProperty("id_estoque")
    private Integer stockId;

    @JsonProperty("nome_almoxarifado")
    private String warehouseName;

    @JsonProperty("cnpj_filial_mega")
    private String warehouseCnpj;

    @JsonProperty("qtde_item_estoque")
    private BigDecimal warehouseItemQuantity;

    @JsonProperty("valor_total_estoque")
    private BigDecimal warehouseTotalValue;
    
    // Construtor explícito para a query JPQL
    public StockDTO(Integer stockId, String warehouseName, String warehouseCnpj, 
                     BigDecimal warehouseItemQuantity, BigDecimal warehouseTotalValue) {
        this.stockId = stockId;
        this.warehouseName = warehouseName;
        this.warehouseCnpj = warehouseCnpj;
        this.warehouseItemQuantity = warehouseItemQuantity;
        this.warehouseTotalValue = warehouseTotalValue;
    }
}