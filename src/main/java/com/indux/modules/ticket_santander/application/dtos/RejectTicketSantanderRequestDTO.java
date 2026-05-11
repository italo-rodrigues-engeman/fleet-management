package com.indux.modules.ticket_santander.application.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectTicketSantanderRequestDTO {
    
    @NotBlank(message = "Observação é obrigatória")
    private String observacao;
}
