package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.AnexoPropostaResponseDTO;
import com.indux.modules.request_budgets.application.dto.BudgetPropostaResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetProposta;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BudgetPropostaMapper {

    public BudgetPropostaResponseDTO toResponseDTO(BudgetProposta proposta) {
        List<AnexoPropostaResponseDTO> anexosDTO = null;
        if (proposta.getAnexos() != null) {
            anexosDTO = proposta.getAnexos().stream()
                    .map(anexo -> AnexoPropostaResponseDTO.builder()
                            .anexoTipo(anexo.getAnexoTipo())
                            .anexoProposta(anexo.getAnexoProposta())
                            .build())
                    .collect(Collectors.toList());
        }

        return BudgetPropostaResponseDTO.builder()
                .id(proposta.getId())
                .versionId(proposta.getVersionId())
                .dataHoraEntrega(proposta.getDataHoraEntrega())
                .valorFinalTotal(proposta.getValorFinalTotal())
                .comprovanteEntrega(proposta.getComprovanteEntrega())
                .anexos(anexosDTO)
                .createdAt(proposta.getCreatedAt())
                .updatedAt(proposta.getUpdatedAt())
                .createdBy(proposta.getCreatedBy())
                .updatedBy(proposta.getUpdatedBy())
                .build();
    }
}

