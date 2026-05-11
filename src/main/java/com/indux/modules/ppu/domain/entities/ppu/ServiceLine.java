package com.indux.modules.ppu.domain.entities.ppu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.domain.entities.item.ServicePosition;
import com.indux.modules.ppu.domain.strategy.types.LineStrategyType;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceLine extends LinePPU{
    @JsonProperty("tipo")
    private ServiceType type;
    private List<ServicePosition> positions;
    @JsonProperty("colaboradores")
    private List<BoardedEmployee> employees;
    @JsonProperty("disposicao")
    private Boolean disposicao;
    @JsonProperty("servicoHoraExtra")
    @Nullable
    private Boolean isOvertimeService;
    @JsonProperty("servicosFilhos")
    @Nullable private List<String> children;
    @JsonProperty("servicoPai")
    @Nullable private String parentId;
    
    /**
     * Tipo da LineStrategy a ser aplicada para cálculo de quantidade/valor.
     * Se não especificado, será selecionada automaticamente baseado no tipo de serviço.
     */
    @JsonProperty("estrategia")
    @Nullable
    private LineStrategyType lineStrategy;
    
    /**
     * Tipo da TimeStrategy a ser aplicada para cálculo de hora extra/adicional noturno.
     * Usado apenas em calculos.
     */
    @JsonProperty("estrategiaHoras")
    @Nullable
    private TimeStrategyType timeStrategy;
    
    /**
     * Percentual de distribuição para strategies particionais (TimeStrategy).
     * Usado quando timeStrategy é OVERTIME_PARTITIONAL, GENERAL_PARTITIONAL ou
     * PREMIUM_NIGHT_PARTITIONAL.
     * 
     * Valor entre 0.0 e 1.0 (ex: 0.5 = 50%)
     */
    @JsonProperty("porcentagemDistribuicao")
    @Nullable
    private Double distributionPercentage;

    @JsonProperty("servicoHoraExtraId")
    @Nullable
    private String overtimeService;
}

