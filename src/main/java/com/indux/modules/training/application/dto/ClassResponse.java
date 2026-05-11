package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;

import java.time.LocalDate;
import java.util.List;

public record ClassResponse(
        String id,
        Long autoId,
        String filialHCM,
        String nomeFilial,
        Long idProjeto,
        String nomeProjeto,
        Long idContrato,
        String nomeContrato,
        Long idRegional,
        String nomeRegional,
        @JsonProperty("instituicao")
        InstituinResponseDTO instituin,
        @JsonProperty("treinamento")
        TrainingResponse training,
        @JsonProperty("dataInicio")
        LocalDate dateStrart,
        @JsonProperty("dataFim")
        LocalDate dateEnd,
        @JsonProperty("finalizado")
        LocalDate finished,
        @JsonProperty("observacao")
        String observation,
        @JsonProperty("tipo")
        String type,
        @JsonProperty("modalidade")
        String modality,
        @JsonProperty("capaxidadeMaxima")
        Double capacityMax,
        @JsonProperty("capacidadeMinima")
        Double capacityMin,
        @JsonProperty("colaboradores")
        List<EmployeeResponseDTO> collaborators,
        Boolean status,
        @JsonProperty("anexoPegar")
        AttachmentEntity attachment
) {
}
