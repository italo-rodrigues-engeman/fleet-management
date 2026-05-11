package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalibrationStandardCreate {
    @JsonProperty("proprietariosId")
    private String proprietiesId;
    @JsonProperty("equipamentoId")
    private String equipmentId;
    @JsonProperty("modelos")
    private List<ModelCreate> models;
    @JsonProperty("faixas")
    private List<RangeCreate> range;
    @JsonProperty("mensagemAprovacao")
    private String messageApproval;
    @JsonProperty("mensagemReprovacao")
    private String messageDisapproval;
    @JsonProperty("status")
    private Boolean status;
    @JsonProperty("dataLog")
    private List<DataLog> dataLog;
}