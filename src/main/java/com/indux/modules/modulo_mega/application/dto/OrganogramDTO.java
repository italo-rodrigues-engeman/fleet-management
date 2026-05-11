package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class OrganogramDTO {

    @JsonProperty("codigoContrato")
    private Integer contractId;

    @JsonProperty("nomeContrato")
    private String contractName;

    @JsonProperty("codigoSetor")
    private Integer sectorId;
    
    @JsonProperty("nomeSetor")
    private String sectorName;

    @JsonProperty("codigoRegional")
    private Integer regionalCode;
    
    @JsonProperty("nomeRegional")
    private String regionalName;

    @JsonProperty("codigoSuperintendencia")
    private Integer superintendenceId;
    
    @JsonProperty("nomeSuperintendencia")
    private String superintendenceName;

    @JsonProperty("codigoDiretoria")
    private Integer directorateId;
    
    @JsonProperty("nomeDiretoria")
    private String directorateName;
}