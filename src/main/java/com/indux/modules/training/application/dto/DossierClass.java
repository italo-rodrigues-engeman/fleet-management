package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;

import java.time.LocalDate;

public record DossierClass(
        String id,
        @JsonProperty("dataInicio")
        LocalDate dateStrart,
        @JsonProperty("dataFim")
        LocalDate dateEnd,
        @JsonProperty("validade")
        LocalDate validity,
        @JsonProperty("cargaHoraria")
        String workload,
        @JsonProperty("situacao")
        String situation,
        @JsonProperty("nota")
        String score,
        @JsonProperty("tipo")
        String type,
        @JsonProperty("certificado")
        AttachmentEntity certification,
        @JsonProperty("anexoPegar")
        AttachmentEntity attachment
) {
}
