package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NiOrganizationCreate(
        @JsonProperty("patrimonio")
        String heritage,
        @JsonProperty("modeloId")
        String standardId,
        @JsonProperty("regionaisId")
        List<Long>branchIds,
        @JsonProperty("contratosId")
        List<Long> contractIds,
        @JsonProperty("projetosId")
        List<Long> projectIds,
        @JsonProperty("propriedadesId")
        List<String> propertiesId,
        @JsonProperty("observacao")
        String observation
) {
}
