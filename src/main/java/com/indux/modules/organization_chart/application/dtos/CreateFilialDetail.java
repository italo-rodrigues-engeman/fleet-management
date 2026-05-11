package com.indux.modules.organization_chart.application.dtos;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateFilialDetail(
        @NotBlank(message = "Item obrigatório") Integer filial,
        @NotBlank(message = "Item obrigatório") Integer ccMega,
        @NotBlank(message = "Item obrigatório") Boolean status

        ) {
}
