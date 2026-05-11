package com.indux.modules.ppu.application.dtos.item;

import com.indux.modules.ppu.application.dtos.rdo.itens.RDOCreate;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;

public record SteelCableControlDTO(
        String id,
        String localizacao,
        String sistema,
        @NotNull(message = "Campo de operacionalidade do cabo de aço não foi preenchido.", groups = RDOCreate.class) Boolean operacional,
        @NotNull(message = "Campo da disponibilidade do cabo de aço não foi preenchido.", groups = RDOCreate.class) Boolean caboReserva,
        @Nullable String responsabilidadeInoperancia,
        @Nullable String justificativaOperacional,
        @Nullable String plataforma
) {
}