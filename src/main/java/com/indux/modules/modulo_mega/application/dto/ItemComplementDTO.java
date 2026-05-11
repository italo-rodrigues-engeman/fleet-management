package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemComplementDTO {
    
    @JsonProperty("idItem")
    private Integer idItem;
    
    @JsonProperty("nomeItem")
    private String itemName;
    
    @JsonProperty("itemComplemento")
    private String itemComplement;
    
    @JsonProperty("status")
    private String status;

    @JsonProperty("previsao_inativacao")
    private LocalDate previsaoInativacao;
}

