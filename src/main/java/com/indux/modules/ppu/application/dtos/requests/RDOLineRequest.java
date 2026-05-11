package com.indux.modules.ppu.application.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.domain.entities.rdo.RDOLine;

public record RDOLineRequest(
        String id,
        @JsonProperty("linhaPai") String parentId,
        @JsonProperty("numeroPai") String parentNumber,
        @JsonProperty("nomePai") String parentName,
        @JsonProperty("quantidade") Double valueMeasured
) {

    public static RDOLine toEntity(RDOLineRequest rdoLineRequest) {
        return new RDOLine(
                rdoLineRequest.id(),
                rdoLineRequest.parentId(),
                rdoLineRequest.parentNumber(),
                rdoLineRequest.parentName(),
                rdoLineRequest.valueMeasured()
        );
    }
}
