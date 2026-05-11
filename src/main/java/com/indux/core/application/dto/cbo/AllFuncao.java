package com.indux.core.application.dto.cbo;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AllFuncao(
        String id,
        @JsonProperty("idAuto") Long autoIncrementId,
        @JsonProperty("dataLog")List<DataLog> dataLog,
        @JsonProperty("regional") List<String> branch,
        @JsonProperty("contrato") List<String> contract,
        @JsonProperty("filialHCM") List<Long> filialHCM
    ) {
}
