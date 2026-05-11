package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalibrationReturn {
    @JsonProperty("id")
    private String id;

    @JsonProperty("dataCalibracao")
    private LocalDate calibrationDate;

    @JsonProperty("laboratorioPadrao")
    private String standardLab;

    @JsonProperty("numeroCertificacao")
    private String nCertification;

    @JsonProperty("periodicidade")
    private Periodicity periodicity;

    @JsonProperty("certificado")
    private List<AttachmentEntity> certification;

    @JsonProperty("padraoCalibracao")
    private List<AttachmentEntity> certificationStandard;

    @JsonProperty("dadosCalibracao")
    private List<CalibrationData> calibrationData;

    @JsonProperty("situacao")
    private Boolean situation;

    @JsonProperty("foto")
    private List<AttachmentEntity> picture;

    @JsonProperty("descricao")
    private String description;

    @JsonProperty("pecas")
    private String parts;

    @JsonProperty("condicao")
    private String condition;

    @JsonProperty("status")
    private String status;

    @JsonProperty("removido")
    private Boolean removed;

    @JsonProperty("dataLogs")
    private List<DataLog> dataLogs;


}