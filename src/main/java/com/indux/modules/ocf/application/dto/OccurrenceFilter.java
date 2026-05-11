package com.indux.modules.ocf.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OccurrenceFilter(
        List<String> statuses,
        List<String> situations,
        List<Integer> currentSteps,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        String id,
        Integer codeId,
        List<String> competencias,
        List<Integer> regionaisIds,
        List<Integer> projectsIds,
        String requesterName,
        String stepLogUserId,
        String complainantId,
        String origin,
        String type,
        String tipoFluxo,
        List<Long> teamIds,
        List<Long> diretoriaIds,
        List<Long> superintendenciaIds,
        List<Long> regionalIds,
        List<Long> setorIds,
        List<Long> contratoIds,
        List<Long> projetoIds,
        List<Long> filialHcmIds
) {
    public OccurrenceFilter withRegionalAndProject(List<Integer> regionais, List<Integer> projects, List<Integer> currentStep) {
        return new OccurrenceFilter(
                statuses,
                situations,
                List.copyOf(currentStep),
                createdFrom,
                createdTo,
                id,
                codeId,
                competencias,
                List.copyOf(regionais),
                List.copyOf(projects),
                requesterName,
                stepLogUserId,
                complainantId,
                origin,
                type,
                tipoFluxo,
                teamIds,
                diretoriaIds,
                superintendenciaIds,
                regionalIds,
                setorIds,
                contratoIds,
                projetoIds,
                filialHcmIds
        );
    }

}

