package com.indux.modules.organization_chart.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.employee.Filial;
import com.indux.modules.organization_chart.domain.entities.jpa.*;

import java.util.List;

public record ProjectDTO(
    long id,
    MegaEntity mega,
    HcmEntity hcm,
    List<FilialHcmEntity> filial,
    @JsonProperty("subordinado") OrganizationEntity subordinate,
    @JsonProperty("contrato") ContractEntity contract,
    @JsonProperty("ativo") Boolean active,
    @JsonProperty("filialMega")Filial branchMega,
    @JsonProperty("tipo") String tipo
) {
}
