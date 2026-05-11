package com.indux.modules.organization_chart.application.dtos;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateProjectDTO(
    @NotBlank(message = "Item obrigatório") Integer mega,
    @NotBlank(message = "Item obrigatório") Integer hcm,
    @NotBlank(message = "Item obrigatório") List<CreateFilialDetail> filial,
    Long subordinado,
    Long contrato,
    Boolean ativo,
    @NotBlank(message = "Item obrigatório") Long filialMega,
    @NotBlank(message = "Item obrigatório") String tipo
) {
}
