package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RejectionSolicitationDTO {
    
    @NotBlank(message = "Justificativa de rejeição é obrigatória")
    @JsonProperty("justificativaRejeicao")
    private String rejectionMessage;
}

