package com.indux.modules.ppu.application.dtos.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.application.dtos.rdo.itens.RDOEmployees;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.ppu.AuditableLineConfig;
import com.indux.modules.ppu.domain.strategy.types.LineStrategyType;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import com.mongodb.lang.Nullable;

import java.util.List;

public record ServiceItemDTO(
        @Nullable String id,
        String numero,
        String numeroPPU,
        String nome,
        String unidadeMedida,
        Double valor,
        Double fator,
        List<String> cargos,
        @Nullable List<BoardedEmployee> colaboradores,
        @Nullable List<RDOEmployees> presencaColaboradores,
        Boolean disposicao,
        List<MeasurementForecast> totalPrevisto,
        Boolean servicoHoraExtra,
        List<String> filhos,
        @Nullable String plataforma,
        String tipo,
        String linhaPai,
        @Nullable String linhaAuditavel,
        @Nullable List<AuditableLineConfig> linhasAuditaveis,
        @JsonProperty("estrategiaLinha") @Nullable LineStrategyType lineStrategy,
        @JsonProperty("estrategiaHoraExtra") @Nullable TimeStrategyType timeStrategy,
        @JsonProperty("porcentagemDistribuida") @Nullable Double distributionPercentage,
        @Nullable String linhaHoraExtraId
) {
}
