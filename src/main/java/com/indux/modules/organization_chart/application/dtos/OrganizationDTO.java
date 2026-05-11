package com.indux.modules.organization_chart.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.employee.Employee;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationSubType;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;

public record OrganizationDTO(
        long id,
        @JsonProperty("sigla") String acronym,
        @JsonProperty("colaborador") Employee collaborator,
        @JsonProperty("obeservacao") String observation,
        @JsonProperty("ativo") Boolean active,
        @JsonProperty("subordinado") OrganizationEntity subordinate,
        @JsonProperty("hierarquia") OrganizationType hierarchy,
        @JsonProperty("cargo") String position,
        @JsonProperty("tipo") OrganizationType type,
        @JsonProperty("subTipo")OrganizationSubType subType
) { }
