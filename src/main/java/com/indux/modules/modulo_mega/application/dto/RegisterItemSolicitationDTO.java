package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterItemSolicitationDTO {
    
    @NotNull(message = "Código do item é obrigatório")
    @JsonProperty("codigoItem")
    private Integer itemCode;
    
    @NotNull(message = "Grupo do item é obrigatório")
    @JsonProperty("grupoItem")
    private Integer itemGroup;
    
    @NotNull(message = "Nome do item é obrigatório")
    @JsonProperty("nomeItem")
    private String itemName;
    
    @JsonProperty("observacaoCadastro")
    private String observationRegistration;
}

