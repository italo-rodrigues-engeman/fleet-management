package com.indux.modules.cdi.aplication.dtos;

import com.indux.modules.cdi.domain.entities.models.Action;
import com.indux.modules.cdi.domain.entities.models.Complexity;

import java.util.Date;
import java.util.List;

public record UpdateCdiDTO (
        Integer gravidade,
        Integer  urgencia,
        Integer tendencia,
        String observacao,
        Complexity prioridade,
        ApplicantDTO realizador,
        Integer pontos,
        Action acao,
        List<CriteriaDTO> criterios,
        EvaluatorDTO avaliador,
        Date prazo
){
}
