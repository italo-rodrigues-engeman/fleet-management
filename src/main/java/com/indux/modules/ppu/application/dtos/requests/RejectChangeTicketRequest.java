package com.indux.modules.ppu.application.dtos.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for rejecting a change ticket.
 */
public record RejectChangeTicketRequest(
        @NotBlank(message = "Motivo da rejeição é obrigatório")
        String motivoRejeicao
) {
}