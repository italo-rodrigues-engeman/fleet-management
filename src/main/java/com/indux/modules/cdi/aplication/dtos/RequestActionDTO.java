package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;

import java.util.Date;
import java.util.List;

public record RequestActionDTO(
        @JsonProperty("dataInicio") Date startDate,
        @JsonProperty("envolvidos")List<EvaluatorDTO> involved,
        @JsonProperty("detalhe") String detail,
        List<String> link,
        @JsonProperty("arquivo") List<AttachmentEntity> file
        ) {
}
