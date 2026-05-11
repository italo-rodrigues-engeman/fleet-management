package com.indux.modules.budgets.application.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.budgets.application.dto.SimpleBudgetResponseDTO;
import com.indux.modules.budgets.application.dto.SimpleBudgetSummaryDTO;
import com.indux.modules.budgets.application.dto.CreateSimpleBudgetRequest;
import com.indux.modules.budgets.application.dto.CreateSimpleBudgetResponseDTO;
import com.indux.modules.budgets.application.mapper.SimpleBudgetMapper;
import com.indux.modules.budgets.domain.model.SimpleBudget;
import com.indux.modules.budgets.domain.model.SimpleBudgetItem;
import com.indux.modules.budgets.domain.model.SimpleBudget;
import com.indux.modules.budgets.domain.model.SimpleBudgetItem;
import com.indux.modules.budgets.domain.model.SimpleBudgetServiceItem;
import com.indux.modules.budgets.domain.model.SimpleBudgetServiceSupplier;
import com.indux.modules.budgets.domain.repository.SimpleBudgetRepository;
import com.indux.modules.budgets.application.dto.AddBudgetServicesRequest;
import com.indux.modules.budgets.application.dto.BudgetServiceItemDTO;
import com.indux.modules.budgets.application.dto.BudgetServiceSupplierDTO;
import com.indux.modules.budgets.application.dto.UpdateServiceSupplierValuesRequest;
import com.indux.modules.budgets.application.dto.ApproveBudgetRequest;
import com.indux.modules.budgets.application.dto.CreateItemGroupRequest;
import com.indux.modules.budgets.application.dto.SimpleBudgetItemGroupResponseDTO;
import com.indux.modules.budgets.domain.model.SimpleBudgetItemGroup;
import com.indux.modules.budgets.domain.repository.SimpleBudgetItemGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.indux.modules.budgets.application.dto.SimpleBudgetFilter;
import com.indux.modules.budgets.application.dto.UpdateBudgetItemsRequest;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.StepLog;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.indux.core.domain.service.AttachmentService;

@Service
@RequiredArgsConstructor
public class SimpleBudgetService {

    private final SimpleBudgetRepository simpleBudgetRepository;
    private final SimpleBudgetMapper simpleBudgetMapper;
    private final AttachmentService attachmentService;
    private final SimpleBudgetItemGroupRepository simpleBudgetItemGroupRepository;

    public CreateSimpleBudgetResponseDTO createBudget(CreateSimpleBudgetRequest request, String userId) {
        if (simpleBudgetRepository.existsByNomeOportunidade(request.getNomeOportunidade())) {
            throw new IllegalArgumentException("Já existe um orçamento com este nome de oportunidade");
        }

        SimpleBudget budget = SimpleBudget.builder()
                .clienteId(request.getClienteId())
                .clienteNome(request.getClienteNome())
                .setorId(request.getSetorId())
                .acOs(request.getAcOs())
                .setor(request.getSetor())
                .orcamentista(request.getOrcamentista())
                .oportunidade(request.getOportunidade())
                .nomeOportunidade(request.getNomeOportunidade())
                .status("ABERTO")
                .step(1)
                .stepLog(new ArrayList<>())
                .build();

        if (userId != null && !userId.isEmpty()) {
                StepLog creationLog = StepLog.builder()
                        .id(UUID.randomUUID())
                        .name("CRIACAO_ORCAMENTO")
                        .user(UUID.fromString(userId))
                        .created_at(Date.from(Instant.now()))
                        .final_at(Date.from(Instant.now()))
                        .step(1)
                        .build();
                budget.getStepLog().add(creationLog);
            
        }

        SimpleBudget savedBudget = simpleBudgetRepository.save(budget);

        return CreateSimpleBudgetResponseDTO.builder()
                .message("Orçamento criado com sucesso")
                .status(201)
                .budgetId(savedBudget.getId())
                .nomeOportunidade(savedBudget.getNomeOportunidade())
                .budgetStatus(savedBudget.getStatus())
                .build();
    }

    public SimpleBudgetResponseDTO getBudgetById(String id) {
        SimpleBudget budget = simpleBudgetRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado com ID: " + id));
        return simpleBudgetMapper.toResponseDTO(budget);
    }

    public List<SimpleBudgetSummaryDTO> getAllBudgets() {
        List<SimpleBudget> budgets = simpleBudgetRepository.findAll();
        return budgets.stream()
                .map(simpleBudgetMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    public Page<SimpleBudgetSummaryDTO> searchBudgets(SimpleBudgetFilter filter, Pageable pageable) {
        return simpleBudgetRepository.search(filter, pageable)
                .map(simpleBudgetMapper::toSummaryDTO);
    }

    public SimpleBudgetResponseDTO updateItems(String id, UpdateBudgetItemsRequest request, String userId) {
        SimpleBudget budget = simpleBudgetRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado com ID: " + id));

        budget.setItens(request.getItens());

        if (userId != null && !userId.isEmpty()) {
                StepLog updateLog = StepLog.builder()
                        .id(UUID.randomUUID())
                        .name("ATUALIZACAO_ITENS")
                        .user(UUID.fromString(userId))
                        .created_at(Date.from(Instant.now()))
                        .final_at(Date.from(Instant.now()))
                        .step(1)
                        
                        .build();
                
                budget.setStep(1);

                if (budget.getStepLog() == null) {
                    budget.setStepLog(new ArrayList<>());
                }
                budget.getStepLog().add(updateLog);

        }

        SimpleBudget savedBudget = simpleBudgetRepository.save(budget);
        return simpleBudgetMapper.toResponseDTO(savedBudget);
    }

    public SimpleBudgetResponseDTO updateItemValues(String id, UpdateBudgetItemsRequest request, String userId) {
        SimpleBudget budget = simpleBudgetRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado con ID: " + id));

        if (budget.getItens() == null) {
            budget.setItens(new ArrayList<>());
        }

        if (request.getItens() != null) {
            for (SimpleBudgetItem updatedItem : request.getItens()) {
                boolean itemFound = budget.getItens().stream()
                        .filter(item -> (updatedItem.getCodMega() != null && updatedItem.getCodMega().equals(item.getCodMega()))
                                || (updatedItem.getNome() != null && updatedItem.getNome().equalsIgnoreCase(item.getNome())))
                        .findFirst()
                        .map(item -> {
                            if (updatedItem.getNome() != null) {
                                item.setNome(updatedItem.getNome());
                            }
                            if (updatedItem.getDescricao() != null) {
                                item.setDescricao(updatedItem.getDescricao());
                            }
                            if (updatedItem.getGrupo() != null) {
                                item.setGrupo(updatedItem.getGrupo());
                            }
                            if (updatedItem.getGrupoItem() != null) {
                                item.setGrupoItem(updatedItem.getGrupoItem());
                            }
                            if (updatedItem.getCodGrupo() != null) {
                                item.setCodGrupo(updatedItem.getCodGrupo());
                            }
                            if (updatedItem.getCodMega() != null) {
                                item.setCodMega(updatedItem.getCodMega());
                            }
                            if (updatedItem.getFase() != null) {
                                item.setFase(updatedItem.getFase());
                            }
                            if (updatedItem.getUnidadeMedida() != null) {
                                item.setUnidadeMedida(updatedItem.getUnidadeMedida());
                            }
                            if (updatedItem.getTipoItem() != null) {
                                item.setTipoItem(updatedItem.getTipoItem());
                            }
                            if (updatedItem.getPrecoMedio() != null) {
                                item.setPrecoMedio(updatedItem.getPrecoMedio());
                            }
                            item.setValorConsiderado(updatedItem.getValorConsiderado());
                            item.setQuantidade(updatedItem.getQuantidade());
                            item.setValorOrcamento(updatedItem.getValorOrcamento());
                            return true;
                        })
                        .orElse(false);

                // Se o item não foi encontrado, adiciona como novo
                if (!itemFound) {
                    budget.getItens().add(updatedItem);
                }
            }
        }

        if (userId != null && !userId.isEmpty()) {
                StepLog updateLog = StepLog.builder()
                        .id(UUID.randomUUID())
                        .name("ATUALIZACAO_VALORES_ITENS")
                        .user(UUID.fromString(userId))
                        .created_at(Date.from(Instant.now()))
                        .final_at(Date.from(Instant.now()))
                        .step(1)
                        .build();
                budget.setStep(1);
                budget.getStepLog().add(updateLog);

        }

        SimpleBudget savedBudget = simpleBudgetRepository.save(budget);
        return simpleBudgetMapper.toResponseDTO(savedBudget);
    }

    public SimpleBudgetResponseDTO addServices(String id, AddBudgetServicesRequest request, String userId) {
        SimpleBudget budget = simpleBudgetRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado com ID: " + id));

        if (budget.getServicos() == null) {
            budget.setServicos(new ArrayList<>());
        }

        if (request.getServicos() != null) {
            for (BudgetServiceItemDTO dto : request.getServicos()) {
                // Busca se o serviço já existe (por nome)
                SimpleBudgetServiceItem existingService = budget.getServicos().stream()
                        .filter(s -> s.getNome() != null && s.getNome().equalsIgnoreCase(dto.getNome()))
                        .findFirst()
                        .orElse(null);

                if (existingService != null) {
                    // Atualiza o serviço existente
                    if (dto.getTipo() != null) {
                        existingService.setTipo(dto.getTipo());
                    }
                    if (dto.getDescricao() != null) {
                        existingService.setDescricao(dto.getDescricao());
                    }
                    if (dto.getFase() != null) {
                        existingService.setFase(dto.getFase());
                    }
                    if (dto.getGrupoItem() != null) {
                        existingService.setGrupoItem(dto.getGrupoItem());
                    }

                    // Atualiza ou adiciona fornecedores
                    if (dto.getFornecedores() != null) {
                        if (existingService.getFornecedores() == null) {
                            existingService.setFornecedores(new ArrayList<>());
                        }

                        for (BudgetServiceSupplierDTO sDto : dto.getFornecedores()) {
                            // Busca se o fornecedor já existe
                            SimpleBudgetServiceSupplier existingSupplier = existingService.getFornecedores().stream()
                                    .filter(f -> f.getFornecedor() != null && f.getFornecedor().equalsIgnoreCase(sDto.getFornecedor()))
                                    .findFirst()
                                    .orElse(null);

                            if (existingSupplier != null) {
                                // Atualiza fornecedor existente
                                if (sDto.getValor() != null) {
                                    existingSupplier.setValor(sDto.getValor());
                                }
                                if (sDto.getUrl() != null) {
                                    existingSupplier.setUrl(sDto.getUrl());
                                }
                                if (sDto.getObservacao() != null) {
                                    existingSupplier.setObservacao(sDto.getObservacao());
                                }
                                if (sDto.getUnidadeMedida() != null) {
                                    existingSupplier.setUnidadeMedida(sDto.getUnidadeMedida());
                                }
                                if (sDto.getSelecionado() != null) {
                                    existingSupplier.setSelecionado(sDto.getSelecionado());
                                }
                                
                                // Substitui anexos se novos forem enviados (evita duplicatas)
                                if (sDto.getAnexos() != null && !sDto.getAnexos().isEmpty()) {
                                    List<AttachmentEntity> newAttachments = attachmentService.createAttachmentsFromMultipartFiles(
                                            sDto.getAnexos(), "budgets/services/attachments");
                                    if (!newAttachments.isEmpty()) {
                                        existingSupplier.setAnexos(newAttachments);
                                    }
                                }
                            } else {
                                // Adiciona novo fornecedor
                                SimpleBudgetServiceSupplier.SimpleBudgetServiceSupplierBuilder builder = SimpleBudgetServiceSupplier.builder()
                                        .fornecedor(sDto.getFornecedor())
                                        .valor(sDto.getValor())
                                        .url(sDto.getUrl())
                                        .observacao(sDto.getObservacao())
                                        .unidadeMedida(sDto.getUnidadeMedida())
                                        .selecionado(sDto.getSelecionado());

                                if (sDto.getAnexos() != null && !sDto.getAnexos().isEmpty()) {
                                    List<AttachmentEntity> attachments = attachmentService.createAttachmentsFromMultipartFiles(
                                            sDto.getAnexos(), "budgets/services/attachments");
                                    if (!attachments.isEmpty()) {
                                        builder.anexos(attachments);
                                    }
                                }

                                existingService.getFornecedores().add(builder.build());
                            }
                        }
                    }
                } else {
                    // Adiciona novo serviço
                    SimpleBudgetServiceItem newService = SimpleBudgetServiceItem.builder()
                            .tipo(dto.getTipo())
                            .nome(dto.getNome())
                            .descricao(dto.getDescricao())
                            .fase(dto.getFase())
                            .grupoItem(dto.getGrupoItem())
                            .build();

                    if (dto.getFornecedores() != null) {
                        List<SimpleBudgetServiceSupplier> suppliers = dto.getFornecedores().stream().map(sDto -> {
                            SimpleBudgetServiceSupplier.SimpleBudgetServiceSupplierBuilder builder = SimpleBudgetServiceSupplier.builder()
                                    .fornecedor(sDto.getFornecedor())
                                    .valor(sDto.getValor())
                                    .url(sDto.getUrl())
                                    .observacao(sDto.getObservacao())
                                    .unidadeMedida(sDto.getUnidadeMedida())
                                    .selecionado(sDto.getSelecionado());

                            if (sDto.getAnexos() != null && !sDto.getAnexos().isEmpty()) {
                                List<AttachmentEntity> attachments = attachmentService.createAttachmentsFromMultipartFiles(
                                        sDto.getAnexos(), "budgets/services/attachments");
                                if (!attachments.isEmpty()) {
                                    builder.anexos(attachments);
                                }
                            }

                            return builder.build();
                        }).collect(Collectors.toList());
                        newService.setFornecedores(suppliers);
                    }

                    budget.getServicos().add(newService);
                }
            }
        }

        if (userId != null && !userId.isEmpty()) {
            StepLog updateLog = StepLog.builder()
                    .id(UUID.randomUUID())
                    .name("ATUALIZACAO_SERVICOS")
                    .user(UUID.fromString(userId))
                    .created_at(Date.from(Instant.now()))
                    .final_at(Date.from(Instant.now()))
                    .step(budget.getStep())
                    .build();

            if (budget.getStepLog() == null) {
                budget.setStepLog(new ArrayList<>());
            }
            budget.getStepLog().add(updateLog);
        }

        SimpleBudget savedBudget = simpleBudgetRepository.save(budget);
        return simpleBudgetMapper.toResponseDTO(savedBudget);
    }


    public SimpleBudgetResponseDTO updateServiceSupplierValues(String id, UpdateServiceSupplierValuesRequest request, String userId) {
        SimpleBudget budget = simpleBudgetRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado com ID: " + id));

        if (budget.getServicos() != null && request.getValores() != null) {
            for (UpdateServiceSupplierValuesRequest.ServiceSupplierValueDTO update : request.getValores()) {
                budget.getServicos().stream()
                        .filter(s -> s.getNome() != null && s.getNome().equalsIgnoreCase(update.getNomeServico()))
                        .findFirst()
                        .ifPresent(service -> {
                            if (service.getFornecedores() != null) {
                                service.getFornecedores().stream()
                                        .filter(f -> f.getFornecedor() != null && f.getFornecedor().equalsIgnoreCase(update.getFornecedor()))
                                        .findFirst()
                                        .ifPresent(supplier -> {
                                            if (update.getSelecionado() != null) {
                                                supplier.setSelecionado(update.getSelecionado());
                                            }
                                            supplier.setValorConsiderado(update.getValorConsiderado());
                                            supplier.setQuantidade(update.getQuantidade());
                                            supplier.setValorOrcamento(update.getValorOrcamento());
                                        });
                            }
                        });
            }
        }

        if (userId != null && !userId.isEmpty()) {
            StepLog updateLog = StepLog.builder()
                    .id(UUID.randomUUID())
                    .name("ATUALIZACAO_VALORES_SERVICOS")
                    .user(UUID.fromString(userId))
                    .created_at(Date.from(Instant.now()))
                    .final_at(Date.from(Instant.now()))
                    .step(budget.getStep())
                    .build();

            if (budget.getStepLog() == null) {
                budget.setStepLog(new ArrayList<>());
            }
            budget.getStepLog().add(updateLog);
        }

        SimpleBudget savedBudget = simpleBudgetRepository.save(budget);
        return simpleBudgetMapper.toResponseDTO(savedBudget);
    }

    public SimpleBudgetResponseDTO approveBudget(String id, ApproveBudgetRequest request, String userId) {
        SimpleBudget budget = simpleBudgetRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado com ID: " + id));

        budget.setStatus("APROVADO");
        budget.setObservacao(request.getObservation());

        if (userId != null && !userId.isEmpty()) {
            StepLog approveLog = StepLog.builder()
                    .id(UUID.randomUUID())
                    .name("APROVACAO_ORCAMENTO")
                    .user(UUID.fromString(userId))
                    .created_at(Date.from(Instant.now()))
                    .final_at(Date.from(Instant.now()))
                    .step(budget.getStep())
                    .build();

            if (budget.getStepLog() == null) {
                budget.setStepLog(new ArrayList<>());
            }
            budget.getStepLog().add(approveLog);
        }

        SimpleBudget savedBudget = simpleBudgetRepository.save(budget);
        return simpleBudgetMapper.toResponseDTO(savedBudget);
    }

    public SimpleBudgetResponseDTO deleteItem(String id, Integer codMega, String userId) {
        SimpleBudget budget = simpleBudgetRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado com ID: " + id));

        if (budget.getItens() == null || budget.getItens().isEmpty()) {
            throw new IllegalArgumentException("O orçamento não possui itens para deletar");
        }

        boolean removed = budget.getItens().removeIf(item -> 
                item.getCodMega() != null && item.getCodMega().equals(codMega));

        if (!removed) {
            throw new ModuleNotFoundFailure("Item não encontrado com código MEGA: " + codMega);
        }

        if (userId != null && !userId.isEmpty()) {
            StepLog deleteLog = StepLog.builder()
                    .id(UUID.randomUUID())
                    .name("EXCLUSAO_ITEM")
                    .user(UUID.fromString(userId))
                    .created_at(Date.from(Instant.now()))
                    .final_at(Date.from(Instant.now()))
                    .step(budget.getStep())
                    .build();

            if (budget.getStepLog() == null) {
                budget.setStepLog(new ArrayList<>());
            }
            budget.getStepLog().add(deleteLog);
        }

        SimpleBudget savedBudget = simpleBudgetRepository.save(budget);
        return simpleBudgetMapper.toResponseDTO(savedBudget);
    }

    public SimpleBudgetResponseDTO unselectServiceSupplier(String id, String serviceName, String supplierName, String userId) {
        SimpleBudget budget = simpleBudgetRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado com ID: " + id));

        if (budget.getServicos() == null || budget.getServicos().isEmpty()) {
            throw new IllegalArgumentException("O orçamento não possui serviços");
        }

        SimpleBudgetServiceItem service = budget.getServicos().stream()
                .filter(s -> s.getNome() != null && s.getNome().equalsIgnoreCase(serviceName))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("Serviço não encontrado com nome: " + serviceName));

        if (service.getFornecedores() == null || service.getFornecedores().isEmpty()) {
            throw new IllegalArgumentException("O serviço não possui fornecedores");
        }

        SimpleBudgetServiceSupplier supplier = service.getFornecedores().stream()
                .filter(f -> f.getFornecedor() != null && f.getFornecedor().equalsIgnoreCase(supplierName))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("Fornecedor não encontrado com nome: " + supplierName));

        supplier.setSelecionado(false);

        if (userId != null && !userId.isEmpty()) {
            StepLog updateLog = StepLog.builder()
                    .id(UUID.randomUUID())
                    .name("DESSELECAO_FORNECEDOR")
                    .user(UUID.fromString(userId))
                    .created_at(Date.from(Instant.now()))
                    .final_at(Date.from(Instant.now()))
                    .step(budget.getStep())
                    .build();

            if (budget.getStepLog() == null) {
                budget.setStepLog(new ArrayList<>());
            }
            budget.getStepLog().add(updateLog);
        }

        SimpleBudget savedBudget = simpleBudgetRepository.save(budget);
        return simpleBudgetMapper.toResponseDTO(savedBudget);
    }
    public SimpleBudgetItemGroupResponseDTO createItemGroup(CreateItemGroupRequest request) {
        if (simpleBudgetItemGroupRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Já existe um grupo com este nome");
        }

        Integer nextId = simpleBudgetItemGroupRepository.findTopByOrderBySequentialIdDesc()
                .map(group -> group.getSequentialId() + 1)
                .orElse(1);

        SimpleBudgetItemGroup group = SimpleBudgetItemGroup.builder()
                .sequentialId(nextId)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        SimpleBudgetItemGroup savedGroup = simpleBudgetItemGroupRepository.save(group);

        return SimpleBudgetItemGroupResponseDTO.builder()
                .id(savedGroup.getId())
                .sequentialId(savedGroup.getSequentialId())
                .name(savedGroup.getName())
                .description(savedGroup.getDescription())
                .build();
    }

    public List<SimpleBudgetItemGroupResponseDTO> getAllItemGroups() {
        return simpleBudgetItemGroupRepository.findAll().stream()
                .map(group -> SimpleBudgetItemGroupResponseDTO.builder()
                        .id(group.getId())
                        .sequentialId(group.getSequentialId())
                        .name(group.getName())
                        .description(group.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteItemGroup(String id) {
        if (!simpleBudgetItemGroupRepository.existsById(id)) {
            throw new ModuleNotFoundFailure("Grupo não encontrado com ID: " + id);
        }
        simpleBudgetItemGroupRepository.deleteById(id);
    }
}
