package com.indux.modules.request_budgets.application.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.request_budgets.application.dto.BudgetResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetResponseDTO;
import com.indux.modules.request_budgets.application.mapper.BudgetMapper;
import com.indux.modules.request_budgets.domain.model.Budget;
import com.indux.modules.request_budgets.domain.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    public CreateBudgetResponseDTO createBudget(CreateBudgetRequest request, String userId) {
        if (budgetRepository.existsByNomeOportunidade(request.getNomeOportunidade())) {
            throw new IllegalArgumentException("Já existe um orçamento com este nome de oportunidade");
        }

        List<com.indux.modules.request_budgets.domain.model.BudgetSolicitante> solicitantes = request.getSolicitantes() != null
                ? request.getSolicitantes().stream()
                        .map(budgetMapper::toSolicitanteEntity)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        List<com.indux.modules.request_budgets.domain.model.BudgetDocumento> documentos = request.getDocumentos() != null
                ? request.getDocumentos().stream()
                        .map(budgetMapper::toDocumentoEntity)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        Budget budget = Budget.builder()
                .clienteId(request.getClienteId())
                .mercadoId(request.getMercadoId())
                .setorId(request.getSetorId())
                .solicitanteNome(request.getSolicitanteNome())
                .solicitanteFuncaoCargo(request.getSolicitanteFuncaoCargo())
                .solicitanteTelefone1(request.getSolicitanteTelefone1())
                .solicitanteTelefone2(request.getSolicitanteTelefone2())
                .solicitanteEmail1(request.getSolicitanteEmail1())
                .solicitanteEmail2(request.getSolicitanteEmail2())
                .solicitanteLocalizacao(request.getSolicitanteLocalizacao())
                .solicitanteObservacao(request.getSolicitanteObservacao())
                .nomeOportunidade(request.getNomeOportunidade())
                .descricaoOportunidade(request.getDescricaoOportunidade())
                .tempoContrato(request.getTempoContrato())
                .porteEstimado(request.getPorteEstimado())
                .dataAbertura(request.getDataAbertura())
                .representanteComercialId(request.getRepresentanteComercialId())
                .detalhesGerais(request.getDetalhesGerais())
                .status(request.getStatus())
                .dataAcompanhamento(request.getDataAcompanhamento())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .solicitantes(solicitantes)
                .documentos(documentos)
                .build();

        Budget.StepLogEmbedded stepLog = new Budget.StepLogEmbedded();
        stepLog.setId(UUID.randomUUID().toString());
        stepLog.setName("qualificação");
        stepLog.setStep(1);
        stepLog.setCreated_at(new Date());
        stepLog.setUser(userId);
        stepLog.setStepCounter(1);
        
        List<Budget.StepLogEmbedded> stepLogList = new ArrayList<>();
        stepLogList.add(stepLog);
        budget.setStepLog(stepLogList);

        Budget savedBudget = budgetRepository.save(budget);

        return CreateBudgetResponseDTO.success(
                "Orçamento criado com sucesso",
                savedBudget.getId(),
                savedBudget.getNomeOportunidade()
        );
    }

    public BudgetResponseDTO getBudgetById(String id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado"));
        return budgetMapper.toResponseDTO(budget);
    }

    public List<BudgetResponseDTO> getAllBudgets() {
        return budgetRepository.findAll().stream()
                .map(budgetMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}

