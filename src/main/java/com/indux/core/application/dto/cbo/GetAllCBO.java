package com.indux.core.application.dto.cbo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.training.application.dto.TrainingResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record GetAllCBO(
        String idCargo,
        String idCBO,
        String codCBO,
        @JsonProperty("nomeCBO") String nameCBO,
        @JsonProperty("regional") List<String> branch,
        @JsonProperty("contrato") List<String> contract,
        @JsonProperty("nomeHCM") List<String> nameHCM,
        String idHCM,
        @JsonProperty("totalPessoas") Long totalPerson,
        String os,
        @JsonProperty("ativo") Long activo,
        @JsonProperty("filialHCM") List<Integer> filial,
        @JsonProperty("treinamentos") List<TrainingResponse> trainings,
        @JsonProperty("treinamentosExigidos") List<RequiredTrainingStraring> requiredTrainings
        ) {
}
