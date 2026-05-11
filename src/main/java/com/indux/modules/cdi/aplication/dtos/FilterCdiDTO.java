package com.indux.modules.cdi.aplication.dtos;

import com.indux.modules.cdi.domain.entities.models.Scope;
import com.indux.modules.cdi.domain.entities.models.Stage;
import com.indux.modules.cdi.domain.entities.models.Status;
import com.indux.modules.cdi.domain.entities.models.Type;

public record FilterCdiDTO(
        Type tipo,
        Scope abrangencia,
        Status status,
        Stage etapa,
        String[] filiais
) {
}