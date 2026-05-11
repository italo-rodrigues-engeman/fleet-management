package com.indux.modules.cdi.aplication.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record CreateActionDTO(
        @NotNull(message = "Item obrigatório") String cdiId,
        @NotNull(message = "Item obrigatório") ApplicantDTO  responsavel,
        @NotNull(message = "Item obrigatório") Date prazo,
        String title,
        Boolean isDev,
        Integer pontos
) {

}
