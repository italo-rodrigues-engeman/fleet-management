package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;

public record DetailActionDTO(
        @JsonProperty("informacao") String information,
        String link,
        @JsonProperty("arquivo") AttachmentEntity file
) {
}
