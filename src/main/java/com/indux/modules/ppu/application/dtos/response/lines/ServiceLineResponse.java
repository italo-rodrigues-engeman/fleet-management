package com.indux.modules.ppu.application.dtos.response.lines;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.item.ServicePosition;
import com.indux.modules.ppu.domain.entities.ppu.AuditableLineConfig;
import com.indux.modules.ppu.domain.entities.ppu.ServiceType;
import com.indux.modules.ppu.domain.strategy.types.LineStrategyType;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceLineResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("numero")
    private String genericNumber;

    @JsonProperty("numeroPPU")
    private String ppuNumber;

    @JsonProperty("nome")
    private String name;

    @JsonProperty("unidadeMedida")
    private String unitOfMeasurement;

    @JsonProperty("valor")
    private Double value;

    @JsonProperty("fator")
    private Double factor;

    @JsonProperty("linhaAuditavel")
    private String auditableLine;

    @JsonProperty("linhasAuditaveis")
    private List<AuditableLineConfig> auditableLines;

    @JsonProperty("tipo")
    private ServiceType type;

    private List<ServicePosition> positions;

    @JsonProperty("colaboradores")
    private List<BoardedEmployee> employees;

    @JsonProperty("disposicao")
    private Boolean disposicao;

    @JsonProperty("totalPrevisto")
    private List<MeasurementForecast> measurementForecasts;

    @JsonProperty("servicoHoraExtra")
    private Boolean isOvertimeService;

    @JsonProperty("servicosFilhos")
    private List<String> children;

    @JsonProperty("servicoPai")
    private String parentId;

    @JsonProperty("plataformas")
    private List<String> platforms;

    @JsonProperty("estrategia")
    @Nullable
    private LineStrategyType lineStrategy;

    @JsonProperty("estrategiaHoras")
    @Nullable
    private TimeStrategyType timeStrategy;

    @JsonProperty("porcentagemDistribuicao")
    @Nullable
    private Double distributionPercentage;

    @JsonProperty("servicoHoraExtraId")
    @Nullable
    private String overtimeService;
}

