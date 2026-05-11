package com.indux.modules.ocf.application.dto;

public record AtendenteContratoFilialDTO(
        Integer contratoRateioId,
        String nomeCentroCustos,
        Long filialId,
        String filialNome,
        String nomeRegionalAtendente
) {}


