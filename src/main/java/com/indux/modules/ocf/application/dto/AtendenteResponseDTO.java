package com.indux.modules.ocf.application.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.indux.core.domain.model.employee.Regional;
import com.indux.modules.ocf.domain.model.Atendente;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AtendenteResponseDTO(
        Long id,
        String email,
        String nome,
        String matricula,
        Long setor,
        String senha,
        Integer accountId,
        AtendenteFuncionarioResumoDTO funcionario,
        // Informações correlacionadas: contrato (nome_centro_custos via rateio) e
        // regional do atendente
        AtendenteContratoFilialDTO contratoFilial,
        Long projectId,
        List<String> regionais) {

    public static AtendenteResponseDTO fromEntity(Atendente atendente) {
        // Resumo do funcionário: apenas contrato (rateio) e filial
        AtendenteFuncionarioResumoDTO funcionarioResumo = null;
        if (atendente.getFuncionario() != null) {
            funcionarioResumo = new AtendenteFuncionarioResumoDTO(
                    atendente.getFuncionario().getContract_id(),
                    atendente.getFuncionario().getBranch_id(),
                    atendente.getFuncionario().getBusinessEmail(),
                    atendente.getProjectId(),
                    atendente.getFuncionario().getFilialIdHcm()
            );
        }

        // Mapeia todas as regionais configuradas para esse atendente
        List<String> regionaisNomes = null;
        if (atendente.getRegionais() != null && !atendente.getRegionais().isEmpty()) {
            regionaisNomes = atendente.getRegionais().stream()
                    .map(Regional::getRegional)
                    .toList();
        }

        // Preferir a primeira regional explícita configurada, senão cair para a da
        // filial/hierarquia
        String nomeRegionalAtendente = null;
        if (regionaisNomes != null && !regionaisNomes.isEmpty()) {
            nomeRegionalAtendente = regionaisNomes.get(0);
        } else {
            nomeRegionalAtendente = atendente.getRegionalFromFilialNome();
        }

        // Montar resumo contrato/filial baseado no funcionário ou nos próprios dados de
        // filial/regional
        AtendenteContratoFilialDTO contratoFilial = null;
        if (funcionarioResumo != null || atendente.getFilialNome() != null || nomeRegionalAtendente != null) {
            contratoFilial = new AtendenteContratoFilialDTO(
                    funcionarioResumo != null ? funcionarioResumo.contratoRateioId() : null,
                    atendente.getContractNomeCentroCustos(),
                    funcionarioResumo != null ? funcionarioResumo.filialId() : null,
                    atendente.getFilialNome(),
                    nomeRegionalAtendente);
        }

        return new AtendenteResponseDTO(
                atendente.getId(),
                atendente.getEmail(),
                atendente.getNome(),
                atendente.getMatricula(),
                atendente.getSetor(),
                atendente.getSenha(),
                atendente.getAccountId(),
                funcionarioResumo,
                // Pode ser null
                contratoFilial,
                atendente.getProjectId(),
                regionaisNomes);
    }
}