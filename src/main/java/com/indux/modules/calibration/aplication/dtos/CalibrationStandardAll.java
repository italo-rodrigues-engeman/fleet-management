package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CalibrationStandardAll(
        String id,
        Long autoIncrementId,
        @JsonProperty("equipamento")
        Equipment equipament,
        @JsonProperty("modelo")
        List<ModelReturn> model,
        @JsonProperty("proprietario")
        Properties properties,
        Boolean status
) {
}