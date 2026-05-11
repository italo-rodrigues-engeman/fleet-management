package com.indux.modules.organization_chart.application.dtos;

import com.indux.modules.organization_chart.domain.entities.models.OrganizationSubType;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrganizationDTO(
        @NotBlank(message = "Item obrigatório.") String cargo,
        @NotBlank(message = "Item obrigatório.") String sigla,
        String observacao,
        @NotBlank(message = "Item obrigatório.") String ativo,
        Long subordinado,
        OrganizationType  hierarquia,
        @NotNull(message = "Item obrigatório.") UUID colaborador,
        @NotNull(message = "Item obrigatório.") OrganizationType tipo,
        OrganizationSubType subTipo
) {}

