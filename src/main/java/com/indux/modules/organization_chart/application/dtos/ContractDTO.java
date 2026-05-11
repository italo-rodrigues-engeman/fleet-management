package com.indux.modules.organization_chart.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.employee.Filial;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.models.ContractType;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;

public record ContractDTO(
        long id,
        @JsonProperty("nome") String name,
        @JsonProperty("apelido") String nickname,
        @JsonProperty ("os")  String os,
        @JsonProperty("tipo") ContractType type,
        @JsonProperty("subordinado") OrganizationEntity subordinate,
        @JsonProperty("hierarquia") OrganizationType hierarchy,
        @JsonProperty("filial") Filial branch
) { }
