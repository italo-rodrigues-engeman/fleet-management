package com.indux.modules.ppu.application.dtos.response;

import org.springframework.data.domain.Page;

public record RDOPageResponse(
        Page<?> page,
        Integer totalApprovePending,
        Integer totalCorrectionPending
) {
}
