package com.indux.modules.ticket_santander.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketSantanderRequestDTO {
    
    private String status;
    private String prioridade;
    private String categoria;
    private String observacoes;
    private String descricao;
}
