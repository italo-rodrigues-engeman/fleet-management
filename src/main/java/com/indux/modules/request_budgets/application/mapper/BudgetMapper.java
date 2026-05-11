package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.BudgetDocumentoDTO;
import com.indux.modules.request_budgets.application.dto.BudgetResponseDTO;
import com.indux.modules.request_budgets.application.dto.BudgetSolicitanteDTO;
import com.indux.modules.request_budgets.domain.model.Budget;
import com.indux.modules.request_budgets.domain.model.BudgetDocumento;
import com.indux.modules.request_budgets.domain.model.BudgetSolicitante;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BudgetMapper {

    public BudgetResponseDTO toResponseDTO(Budget budget) {
        return BudgetResponseDTO.builder()
                .id(budget.getId())
                .clienteId(budget.getClienteId())
                .mercadoId(budget.getMercadoId())
                .setorId(budget.getSetorId())
                .solicitanteNome(budget.getSolicitanteNome())
                .solicitanteFuncaoCargo(budget.getSolicitanteFuncaoCargo())
                .solicitanteTelefone1(budget.getSolicitanteTelefone1())
                .solicitanteTelefone2(budget.getSolicitanteTelefone2())
                .solicitanteEmail1(budget.getSolicitanteEmail1())
                .solicitanteEmail2(budget.getSolicitanteEmail2())
                .solicitanteLocalizacao(budget.getSolicitanteLocalizacao())
                .solicitanteObservacao(budget.getSolicitanteObservacao())
                .solicitantes(budget.getSolicitantes().stream()
                        .map(this::toSolicitanteDTO)
                        .collect(Collectors.toList()))
                .nomeOportunidade(budget.getNomeOportunidade())
                .descricaoOportunidade(budget.getDescricaoOportunidade())
                .tempoContrato(budget.getTempoContrato())
                .porteEstimado(budget.getPorteEstimado())
                .dataAbertura(budget.getDataAbertura())
                .representanteComercialId(budget.getRepresentanteComercialId())
                .detalhesGerais(budget.getDetalhesGerais())
                .documentos(budget.getDocumentos().stream()
                        .map(this::toDocumentoDTO)
                        .collect(Collectors.toList()))
                .status(budget.getStatus())
                .dataAcompanhamento(budget.getDataAcompanhamento())
                .stepLog(budget.getStepLog() != null ? budget.getStepLog().stream()
                        .map(this::toStepLogDTO)
                        .collect(Collectors.toList()) : null)
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .createdBy(budget.getCreatedBy())
                .updatedBy(budget.getUpdatedBy())
                .build();
    }

    public BudgetSolicitanteDTO toSolicitanteDTO(BudgetSolicitante solicitante) {
        return BudgetSolicitanteDTO.builder()
                .nome(solicitante.getNome())
                .funcaoCargo(solicitante.getFuncaoCargo())
                .telefone1(solicitante.getTelefone1())
                .telefone2(solicitante.getTelefone2())
                .email1(solicitante.getEmail1())
                .email2(solicitante.getEmail2())
                .localizacao(solicitante.getLocalizacao())
                .observacao(solicitante.getObservacao())
                .build();
    }

    public BudgetDocumentoDTO toDocumentoDTO(BudgetDocumento documento) {
        return BudgetDocumentoDTO.builder()
                .tipo(documento.getTipo())
                .maisInformacoes(documento.getMaisInformacoes())
                .descricao(documento.getDescricao())
                .anexos(documento.getAnexos())
                .build();
    }

    public BudgetSolicitante toSolicitanteEntity(BudgetSolicitanteDTO dto) {
        return BudgetSolicitante.builder()
                .nome(dto.getNome())
                .funcaoCargo(dto.getFuncaoCargo())
                .telefone1(dto.getTelefone1())
                .telefone2(dto.getTelefone2())
                .email1(dto.getEmail1())
                .email2(dto.getEmail2())
                .localizacao(dto.getLocalizacao())
                .observacao(dto.getObservacao())
                .build();
    }

    public BudgetDocumento toDocumentoEntity(BudgetDocumentoDTO dto) {
        return BudgetDocumento.builder()
                .tipo(dto.getTipo())
                .maisInformacoes(dto.getMaisInformacoes())
                .descricao(dto.getDescricao())
                .anexos(dto.getAnexos())
                .build();
    }

    private BudgetResponseDTO.StepLogEmbeddedDTO toStepLogDTO(Budget.StepLogEmbedded stepLog) {
        return BudgetResponseDTO.StepLogEmbeddedDTO.builder()
                .id(stepLog.getId())
                .name(stepLog.getName())
                .step(stepLog.getStep())
                .created_at(stepLog.getCreated_at())
                .user(stepLog.getUser())
                .observation(stepLog.getObservation())
                .stepCounter(stepLog.getStepCounter())
                .build();
    }
}

