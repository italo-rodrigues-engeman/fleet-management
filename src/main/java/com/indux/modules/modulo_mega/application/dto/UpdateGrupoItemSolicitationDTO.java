package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGrupoItemSolicitationDTO {
    
    @NotNull(message = "Grupo item é obrigatório")
    @JsonProperty("grupoItem")
    private Integer itemGroup;
    
    @JsonProperty("nomeGrupo")
    private String itemGroupName;
}
