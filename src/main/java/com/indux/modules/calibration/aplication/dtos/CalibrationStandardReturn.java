package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CalibrationStandardReturn(
        String id,
        Long autoIncrementId,
        @JsonProperty("proprietario")
        Properties properties,
        @JsonProperty("equipamento")
        Equipment equipament,
        @JsonProperty("modelos")
        List<ModelReturn> model,
        @JsonProperty("faixa")
        List<RangeReturn> range,
        @JsonProperty("mensagemAprovacao")
        String messageApproval,
        @JsonProperty("mensagemReprovacao")
        String messageDisapproval,
        @JsonProperty("status")
        Boolean status,
        @JsonProperty("dataLog")
        List<DataLog> dataLog
    ) {
    }