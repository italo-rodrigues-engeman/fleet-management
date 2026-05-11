package com.indux.modules.request_budgets.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(collection = "budget_scoring_config")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetScoringConfig {
    @Id
    private String id;

    // value -> score
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

    private UUID createdBy;
    private UUID updatedBy;
}


