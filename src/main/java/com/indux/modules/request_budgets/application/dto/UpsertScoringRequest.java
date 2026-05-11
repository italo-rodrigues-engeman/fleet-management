package com.indux.modules.request_budgets.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpsertScoringRequest {
    private List<ScoringEntryDTO> keywords;
    private List<ScoringEntryDTO> mercado;
    private List<ScoringEntryDTO> setor;
    private List<ScoringEntryDTO> tempoContrato;
    private List<ScoringEntryDTO> porteEstimado;
    private List<ScoringEntryDTO> modalidadeConcorrencia;
    private List<ScoringEntryDTO> tipo;
    private List<ScoringEntryDTO> caracteristica;
    private List<ScoringEntryDTO> estados;
    private Integer pontuacaoReferencia;
}


