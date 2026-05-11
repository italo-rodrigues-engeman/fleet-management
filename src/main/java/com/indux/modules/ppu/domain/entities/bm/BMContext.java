package com.indux.modules.ppu.domain.entities.bm;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BMContext {
    private final LocalDate start;
    private final LocalDate end;
    private final String platform;
    private final Long project;
    private final boolean allPlatforms;
    
    private int daysInQuery;
    private long totalDays;
    private List<String> platforms;
    private Map<String, Long> counts;
    private int currentMeasurementDay;
    private LocalDate measurementStartDate;
    private LocalDate measurementEndDate;
    private BigDecimal coeficienteReajusteContratual;
    Map<String, Double> serviceTotals;

    public BMContext(LocalDate start, LocalDate end, String platform, Long project) {
        this.start = start;
        this.end = end;
        this.platform = platform;
        this.project = project;
        this.allPlatforms = "*".equals(platform);
    }
}