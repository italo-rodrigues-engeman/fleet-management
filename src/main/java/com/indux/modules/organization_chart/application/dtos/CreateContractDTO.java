package com.indux.modules.organization_chart.application.dtos;

import com.indux.modules.organization_chart.domain.entities.models.ContractType;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateContractDTO(
        @NotBlank(message = "Item obriagatório") String nome,
        @Nullable String apelido,
        @NotNull(message = "Item obriagatório") String os,
        @NotNull(message = "Item obriagatório") ContractType tipo,
        @NotNull(message = "Item obriagatório") Long subordinado,
        @NotNull(message = "Item obrigatório.") OrganizationType hierarquia,
        @Nullable Long filial
        ) {}
