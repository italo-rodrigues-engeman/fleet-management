package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalibrationCreate {
    @JsonProperty("id")
    private String id;

    private String idOrganization;

    @JsonProperty("dataCalibracao")
    private LocalDate calibrationDate;

    @JsonProperty("laboratorioPadrao")
    private String standardLab;

    @JsonProperty("numeroCertificacao")
    private String nCertification;

    @JsonProperty("periodicidade")
    private Periodicity periodicity;

    @JsonProperty("certificado")
    private List<MultipartFile> certification;

    @JsonProperty("padraoCalibracao")
    private List<MultipartFile> certificationStandard;

    @JsonProperty("dadosCalibracao")
    private List<CalibrationData> calibrationData;

    @JsonProperty("situacao")
    private Boolean situation;

    @JsonProperty("foto")
    private List<MultipartFile> picture;

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