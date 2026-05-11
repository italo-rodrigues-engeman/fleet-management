package com.indux.modules.organization_chart.application.dtos;

public record ContractSummaryDTO(
        Long id,
        String nome,
        String os,
        String subordinadoSigla,
        String subordinadoTipo
) { }


