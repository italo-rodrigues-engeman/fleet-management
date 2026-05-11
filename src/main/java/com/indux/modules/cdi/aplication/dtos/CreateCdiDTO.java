package com.indux.modules.cdi.aplication.dtos;

import com.indux.modules.cdi.domain.entities.models.*;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record CreateCdiDTO(
        @NotNull(message = "Item obriagatório") ApplicantDTO solicitante,
        @NotNull(message = "Item obriagatório") Type tipo,
        @NotNull(message = "Item obriagatório") Scope abrangencia,
        @NotNull(message = "Item obriagatório") Complexity complexidade,
        @NotNull(message = "Item obriagatório") PrevistTime tempoPrevisto,
        @NotNull(message = "Item obriagatório") String descricao,
        @NotNull(message = "Item obriagatório") Boolean independente,
        @Nullable DetailsDTO detalhe,
        @NotNull(message = "Item obriagatório") String problema,
        @NotNull(message = "Item obriagatório") String resultado,
        @Nullable EspecificResultDTO resultadoEspecifico,
        @NotNull(message = "Item obriagatório") String titulo,
        Status status,
        Stage etapa
) {
}
