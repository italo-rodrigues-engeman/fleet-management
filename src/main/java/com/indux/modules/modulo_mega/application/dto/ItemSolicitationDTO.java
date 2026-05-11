package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemSolicitationDTO {
    
    @NotBlank(message = "Nome principal é obrigatório")
    @JsonProperty("nomePrincipal")
    private String mainName;
    
    @JsonProperty("urlReferencia")
    private String urlReferency;
    
    @JsonProperty("descricao")
    private String description;
    
    @JsonProperty("tipo")
    private String tipo;
    
    @JsonProperty("unidadeMedida")
    private String unidadeMedida;
}

