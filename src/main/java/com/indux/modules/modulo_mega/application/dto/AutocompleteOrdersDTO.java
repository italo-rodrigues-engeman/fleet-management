package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutocompleteOrdersDTO {
    
    @JsonProperty("codFornecedor")
    private Integer supplierCode;
    
    @JsonProperty("fornecedor")
    private String supplierName;
}

