package com.indux.modules.ocf.application.dto;

import com.indux.core.domain.model.employee.ContractProject;

import java.time.LocalDate;
import java.util.List;

public record ContratoSimplificadoDTO(
        Integer id,
        Integer rateio,
        String costCenterName,
        Integer megaId,
        String projectName,
        String contractManager,
        String client,
        Long idCliente,
        String codeSap,
        LocalDate dataAssinatura,
        LocalDate dataFim,
        Boolean ativo,
        List<String> contractCoordinators
) {
    
    public static ContratoSimplificadoDTO fromContractProject(ContractProject contract) {
        return new ContratoSimplificadoDTO(
                contract.getId(),
                contract.getRateio(),
                contract.getCostCenterName(),
                contract.getMegaId(),
                contract.getProjectName(),
                contract.getContractManager(),
                contract.getClient(),
                contract.getIdCliente(),
                contract.getCodeSap(),
                contract.getDataAssinatura(),
                contract.getDataFim(),
                contract.getAtivo(),
                contract.getContractCoordinators()
        );
    }
} 