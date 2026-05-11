package com.indux.modules.ppu.domain.entities.rdo;

import com.indux.modules.ppu.application.dtos.rdo.itens.RDOCreate;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record EquipmentChecker(
        @NotNull(message = "Campo obrigatório para criação do RDO", groups = RDOCreate.class)
        String id,
        @NotNull(message = "Campo obrigatório para criação do RDO", groups = RDOCreate.class)
        String numeroPatrimonio,
        @NotNull(message = "Campo obrigatório para criação do RDO", groups = RDOCreate.class)
        String fabricante,
        @NotNull(message = "Campo obrigatório para criação do RDO", groups = RDOCreate.class)
        String descricaoModelo,
        @NotNull(message = "Campo obrigatório para criação do RDO", groups = RDOCreate.class)
        Boolean operacional,
        @Nullable String observacao
) {
}
