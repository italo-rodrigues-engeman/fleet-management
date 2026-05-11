package com.indux.modules.ppu.application.dtos.requests;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateRDOCoordinatorRequest(
        @NotNull(message = "Não é possível gerar uma PPU sem a plataforma.") String plataforma,
        @NotNull(message = "Não é possível gerar uma PPU sem a data.") LocalDate data,
        @NotNull(message = "Não é possível gerar uma PPU sem o ID do contrato.") Long contratoID,
        CreateRDOJustification justificativa,
        String descricao
) {
}
