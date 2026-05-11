package com.indux.modules.ppu.application.dtos.item;

import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.modules.ppu.application.dtos.rdo.itens.RDOCreate;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CraneControlDTO(
        String id,
        String localizacao,
        String ultimaLubrificacao,
        @NotNull(message = "Campo de operacionalidade do controle de guindaste não foi preenchido.", groups = RDOCreate.class) Boolean operacional,
        @NotNull(message = "Campo de horímetro do controle de guindaste não foi preenchido.", groups = RDOCreate.class) String horimetro,
        @NotNull(message = "Campo de Sistemas não foi preenchido.", groups = RDOCreate.class) List<String> sistemas,
        @NotNull(message = "Campo de checklist não foi preenchido.", groups = RDOCreate.class) Boolean checkList,
        @Nullable List<SimpleEmployeeDTO> colaboradorChecklist,
        @Nullable String responsabilidadeInoperancia,
        @Nullable String justificativaOperacional,
        @Nullable String plataforma
        ) {
}
