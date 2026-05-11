package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;

import java.time.LocalDate;

public record ProposalResponseDTO(
        @JsonProperty("data")
        LocalDate date,
        @JsonProperty("nome")
        String name,
        @JsonProperty("obs")
        String observation,
        @JsonProperty("anexoPegar")
        AttachmentEntity attachment
) {
}
