package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicalValidationSolicitationDTO {
    
    @JsonProperty("codigoItem")
    private Integer itemCode;
    
    @JsonProperty("grupoItem")
    private Integer itemGroup;
    
    @JsonProperty("nomeGrupo")
    private String itemGroupName;
    
    @JsonProperty("nomeItem")
    private String itemName;
    
    @JsonProperty("tipo")
    private String tipo;
    
    @JsonProperty("nomePrincipal")
    private String mainName;
    
    @JsonProperty("unidadeMedida")
    private String unidadeMedida;
    
    @JsonProperty("urlReferencia")
    private String urlReferency;
    
    @JsonProperty("descricao")
    private String description;
}

