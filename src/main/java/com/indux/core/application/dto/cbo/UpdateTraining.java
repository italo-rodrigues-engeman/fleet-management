package com.indux.core.application.dto.cbo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UpdateTraining(
    @JsonProperty("treinamentoId")
    List<String> trainingId,
    @JsonProperty("treinamentosExigidos")
    List<RequiredTrainingStraring> requiredTrainings,
    @JsonProperty("idCbo")
    String cboId,
    @JsonProperty("idHcm")
    String hcmId,
    @JsonProperty("nomeHCM")
    String nameHcm,
    @JsonProperty("filialHCM")
    List<Long> filialHCM,
    @JsonProperty("nomeCBO")
    String nameCBOChildren,
    @JsonProperty("codCBO")
    String codCBOChildren
) {
}
