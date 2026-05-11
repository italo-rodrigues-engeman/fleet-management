package com.indux.modules.request_budgets.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetScoringConfigResponseDTO {
    private String id;
    private Map<String, Integer> keywordScores;
    private Map<String, Integer> mercadoScores;
    private Map<String, Integer> setorScores;
    private Map<String, Integer> tempoContratoScores;
    private Map<String, Integer> porteEstimadoScores;
    private Map<String, Integer> modalidadeConcorrenciaScores;
    private Map<String, Integer> tipoScores;
    private Map<String, Integer> caracteristicaScores;
    private Map<String, Integer> estadoScores;
    private Integer pontuacaoReferencia;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}


