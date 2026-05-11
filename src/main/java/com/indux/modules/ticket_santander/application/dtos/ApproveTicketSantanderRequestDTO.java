package com.indux.modules.ticket_santander.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveTicketSantanderRequestDTO {
    
    private String observacao;
}