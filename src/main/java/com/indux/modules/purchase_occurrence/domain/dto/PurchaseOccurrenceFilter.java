package com.indux.modules.purchase_occurrence.domain.dto;

import java.time.Instant;
import java.util.List;

public record PurchaseOccurrenceFilter(
        List<String> status,
        List<String> situacoes,
        List<Integer> etapasAtuais,
        Instant criadoDe,
        Instant criadoAte,
        String id,
        List<Integer> regionais,
        List<Integer> projetos,
        String nomeComprador,
        String solicitante,
        List<String> causas
) {

    public PurchaseOccurrenceFilter withRegionaisAndProjects(List<Integer> regionais, List<Integer> projects, List<Integer> currentStep) {
        return new PurchaseOccurrenceFilter(
                status,
                situacoes,
                List.copyOf(currentStep),
                criadoDe,
                criadoAte,
                id,
                List.copyOf(regionais),
                List.copyOf(projects),
                nomeComprador,
                solicitante,
                causas
        );
        }
}
