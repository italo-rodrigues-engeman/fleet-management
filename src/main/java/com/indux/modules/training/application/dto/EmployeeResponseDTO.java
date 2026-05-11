package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;

public record EmployeeResponseDTO(
        String id,
        @JsonProperty("nome")
        String name,
        @JsonProperty("matricula")
        String registration,
        @JsonProperty("cargo")
        String position,
        @JsonProperty("situacao")
        String situation,
        @JsonProperty("nota")
        String score,
        @JsonProperty("anexoPegar")
        AttachmentEntity attachment
) {
}
