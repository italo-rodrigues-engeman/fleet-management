package com.indux.modules.ppu.application.dtos.requests;

import com.indux.core.domain.model.modules.AttachmentRecord;
import com.indux.modules.ppu.domain.entities.item.RDORejectionType;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RDOFlowRequest(
        @NotNull @NotBlank String rdo,
        @Nullable String justificativa,
        @Nullable List<AttachmentRecord> anexos,
        @Nullable List<RDORejectionType> motivos,
        @Nullable List<DivergenceRecord> divergencias
) {
}
