package com.indux.modules.ppu.application.dtos.requests;

import java.time.YearMonth;

public record ClosedCompetenceRequest(
        YearMonth competencia,
        Long projeto,
        String observacoes,
        Boolean checado
){ 
    
}
