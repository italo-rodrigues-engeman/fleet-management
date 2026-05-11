package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public record InstituinRequestDTO(
        String id,
        @RequestParam("nome")
        String name,
        @RequestParam("cnpj")
        String cnpj,
        @RequestParam("unidades")
        List<UnitDTO> units,
        @RequestParam("treinamentosId")
        List<String> trainingId,
        @RequestParam("treinamentosNome")
        List<String> trainingName,
        @RequestParam("propostas")
        List<ProposalRequestDTO> proposals
) {
}
