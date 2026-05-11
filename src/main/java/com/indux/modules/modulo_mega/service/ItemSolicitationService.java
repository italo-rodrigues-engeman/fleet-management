package com.indux.modules.modulo_mega.service;

import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.notifcation.mailsender.CustomMailSender;
import com.indux.modules.modulo_mega.application.dto.*;
import com.indux.modules.modulo_mega.application.dto.UpdateGrupoItemSolicitationDTO;
import com.indux.modules.modulo_mega.domain.entities.mongo.ItemSolicitationEntity;
import com.indux.modules.modulo_mega.domain.enums.ItemSolicitationStatus;
import com.indux.modules.modulo_mega.domain.repository.mongo.ItemSolicitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemSolicitationService {

    private final ItemSolicitationRepository itemSolicitationRepository;
    private final UserService userService;
    private final CustomMailSender mailSender;
    private final SequenceGeneratorService sequenceGeneratorService;

    public ItemSolicitationEntity create(ItemSolicitationDTO dto, String regional, UUID userId) {
        ItemSolicitationEntity entity = ItemSolicitationEntity.builder()
                .mainName(dto.getMainName())
                .urlReferency(dto.getUrlReferency())
                .description(dto.getDescription())
                .tipo(dto.getTipo())
                .unidadeMedida(dto.getUnidadeMedida())
                .regional(regional)
                .status(ItemSolicitationStatus.TECHNICAL_VALIDATION)
                .etapa(2)
                .sequentialId(sequenceGeneratorService.generateSequence("item_solicitacao_sequence"))
                .build();

        // Adiciona log de criação com status de validação técnica
        StepLog creationLog = createStepLog(
                userId,
                "CRIACAO_SOLICITACAO",
                2,
                "Solicitação criada com status de validação técnica. Item: " + dto.getMainName()
        );
        addLog(entity, creationLog);

        return itemSolicitationRepository.save(entity);
    }

    public ItemSolicitationPagedResponseDTO findAll(
            Pageable pageable, LocalDate dataInicial, LocalDate dataFinal,
            ItemSolicitationStatus status, String createdBy,
            String mainName, String description) {

        Page<ItemSolicitationEntity> entities;

        // Verifica se algum filtro foi passado
        boolean hasFilters = dataInicial != null || dataFinal != null || status != null
                || (createdBy != null && !createdBy.isBlank())
                || (mainName != null && !mainName.isBlank())
                || (description != null && !description.isBlank());

        if (hasFilters) {
            entities = itemSolicitationRepository.findAllWithFilters(
                    dataInicial, dataFinal, status, createdBy, mainName, description, pageable
            );
        } else {
            entities = itemSolicitationRepository.findAll(pageable);
        }

        Page<ItemSolicitationListDTO> pageDTO = entities.map(entity -> ItemSolicitationListDTO.builder()
                .id(entity.getId())
                .mainName(entity.getMainName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .regional(entity.getRegional())
                .applicant(entity.getCreatedBy())
                .creationDate(entity.getCreationDate())
                .etapa(entity.getEtapa())
                .sequentialId(entity.getSequentialId())
                .build());

        // As contagens permanecem iguais
        long countTechnical = itemSolicitationRepository.countByStatus(ItemSolicitationStatus.TECHNICAL_VALIDATION);
        long countTax = itemSolicitationRepository.countByStatus(ItemSolicitationStatus.TAX_VALIDATION);
        long countRegistration = itemSolicitationRepository.countByStatus(ItemSolicitationStatus.REGISTRATION);
        long countRejected = itemSolicitationRepository.countByStatus(ItemSolicitationStatus.REJECTED);
        long countRegistered = itemSolicitationRepository.countByStatus(ItemSolicitationStatus.REGISTERED);

        return ItemSolicitationPagedResponseDTO.builder()
                .page(pageDTO)
                .countTechnicalValidation(countTechnical)
                .countTaxValidation(countTax)
                .countRegistration(countRegistration)
                .countRejected(countRejected)
                .countRegistered(countRegistered)
                .build();
    }

    public ItemSolicitationEntity findById(String id) {
        return itemSolicitationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitação não encontrada com ID: " + id
                ));
    }

    public ItemSolicitationEntity registerItem(String id, RegisterItemSolicitationDTO dto, UUID userId) {
        ItemSolicitationEntity entity = findById(id);

        // Atualiza os campos de cadastro
        entity.setItemCode(dto.getItemCode());
        entity.setItemGroup(dto.getItemGroup());
        entity.setItemName(dto.getItemName());
        entity.setObservationRegistration(dto.getObservationRegistration());
        entity.setStatus(ItemSolicitationStatus.REGISTERED);

        // Adiciona log de cadastro
        StepLog registerLog = createStepLog(
                userId,
                "CADASTRO_ITEM",
                3,
                "Item cadastrado: " + dto.getItemName() + " (Código: " + dto.getItemCode() + ")"
        );
        addLog(entity, registerLog);

        ItemSolicitationEntity saved = itemSolicitationRepository.save(entity);

        // Envia email para o usuário que criou a solicitação
        sendRegistrationEmail(saved, dto.getItemCode(), dto.getItemName());

        return saved;
    }

    public ItemSolicitationEntity reject(String id, RejectionSolicitationDTO dto, UUID userId) {
        ItemSolicitationEntity entity = findById(id);

        // Atualiza os campos de rejeição
        entity.setRejectionMessage(dto.getRejectionMessage());

        boolean isReturn = false;
        if (entity.getEtapa() != null && entity.getEtapa() == 4) {
            entity.setStatus(ItemSolicitationStatus.TECHNICAL_VALIDATION);
            entity.setEtapa(2);
            isReturn = true;
        } else {
            entity.setStatus(ItemSolicitationStatus.REJECTED);
            entity.setEtapa(0);
        }

        // Adiciona log de rejeição ou devolução
        StepLog rejectionLog = createStepLog(
                userId,
                isReturn ? "DEVOLUCAO_SOLICITACAO" : "REJEICAO_SOLICITACAO",
                entity.getEtapa(),
                (isReturn ? "Solicitação devolvida para correção" : "Solicitação rejeitada") + ". Justificativa: " + dto.getRejectionMessage()
        );
        addLog(entity, rejectionLog);

        ItemSolicitationEntity saved = itemSolicitationRepository.save(entity);

        // Envia email para o usuário que criou a solicitação apenas se for rejeição (não devolução)
        if (!isReturn) {
            sendRejectionEmail(entity, dto.getRejectionMessage());
        }

        return saved;
    }

    private void sendRejectionEmail(ItemSolicitationEntity entity, String motivoRejeicao) {
        try {
            // Obtém o email do usuário que criou a solicitação
            if (entity.getCreatedBy() != null && !entity.getCreatedBy().isBlank()) {
                userService.getUserById(entity.getCreatedBy())
                        .ifPresentOrElse(
                                user -> {
                                    String email = user.getEmail();
                                    String nomeItem = entity.getMainName() != null ? entity.getMainName() : "Item solicitado";
                                    String nomeUsuario = user.getNome() != null ? user.getNome() : "Usuário";

                                    // Monta o corpo do email em HTML
                                    String bodyHtml = buildRejectionEmailBody(nomeUsuario, nomeItem, motivoRejeicao);

                                    // Envia o email
                                    mailSender.sendGenericEmail(
                                            email,
                                            "Solicitação de Item Rejeitada",
                                            bodyHtml,
                                            null
                                    );
                                },
                                () -> log.warn("Usuário não encontrado para enviar email de rejeição. ID: {}", entity.getCreatedBy())
                        );
            } else {
                log.warn("Solicitação não possui createdBy, não é possível enviar email de rejeição. ID: {}", entity.getId());
            }
        } catch (Exception e) {
            log.error("Erro ao enviar email de rejeição para solicitação {}: {}", entity.getId(), e.getMessage(), e);
            // Não lança exceção para não interromper o fluxo de rejeição
        }
    }



    private void sendRegistrationEmail(ItemSolicitationEntity entity, Integer codigoItem, String nomeItem) {
        try {
            // Obtém o email do usuário que criou a solicitação
            if (entity.getCreatedBy() != null && !entity.getCreatedBy().isBlank()) {
                userService.getUserById(entity.getCreatedBy())
                        .ifPresentOrElse(
                                user -> {
                                    String email = user.getEmail();
                                    String nomeItemSolicitado = entity.getMainName() != null ? entity.getMainName() : "Item solicitado";
                                    String nomeUsuario = user.getNome() != null ? user.getNome() : "Usuário";

                                    // Monta o corpo do email em HTML
                                    String bodyHtml = buildRegistrationEmailBody(nomeUsuario, nomeItemSolicitado, codigoItem, nomeItem);

                                    // Envia o email
                                    mailSender.sendGenericEmail(
                                            email,
                                            "Item Cadastrado com Sucesso",
                                            bodyHtml,
                                            null
                                    );
                                },
                                () -> log.warn("Usuário não encontrado para enviar email de cadastro. ID: {}", entity.getCreatedBy())
                        );
            } else {
                log.warn("Solicitação não possui createdBy, não é possível enviar email de cadastro. ID: {}", entity.getId());
            }
        } catch (Exception e) {
            log.error("Erro ao enviar email de cadastro para solicitação {}: {}", entity.getId(), e.getMessage(), e);
            // Não lança exceção para não interromper o fluxo de cadastro
        }
    }

    private String buildRejectionEmailBody(String nomeUsuario, String nomeItem, String motivoRejeicao) {
        return String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                        "<h2 style='color: #d32f2f;'>Solicitação de Item Rejeitada</h2>" +
                        "<p>Olá <strong>%s</strong>,</p>" +
                        "<p>Informamos que sua solicitação de item foi <strong>rejeitada</strong>.</p>" +
                        "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                        "<p><strong>Item solicitado:</strong> %s</p>" +
                        "<p><strong>Motivo da rejeição:</strong></p>" +
                        "<p style='margin-left: 20px; color: #666;'>%s</p>" +
                        "</div>" +
                        "<p>Se tiver dúvidas ou precisar de mais informações, entre em contato com o suporte.</p>" +
                        "<p>Atenciosamente,<br>Equipe Kogni</p>" +
                        "</div>",
                nomeUsuario,
                nomeItem,
                motivoRejeicao != null ? motivoRejeicao.replace("\n", "<br>") : "Não informado"
        );
    }



    private String buildRegistrationEmailBody(String nomeUsuario, String nomeItemSolicitado, Integer codigoItem, String nomeItem) {
        return String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                        "<h2 style='color: #2e7d32;'>Item Cadastrado com Sucesso</h2>" +
                        "<p>Olá <strong>%s</strong>,</p>" +
                        "<p>Informamos que sua solicitação de item foi <strong>cadastrada com sucesso</strong> no sistema.</p>" +
                        "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                        "<p><strong>Item solicitado:</strong> %s</p>" +
                        "<p><strong>Código do item:</strong> <span style='color: #1976d2; font-weight: bold;'>%d</span></p>" +
                        "<p><strong>Nome do item cadastrado:</strong> %s</p>" +
                        "</div>" +
                        "<p>O item já está disponível para uso no sistema.</p>" +
                        "<p>Se tiver dúvidas ou precisar de mais informações, entre em contato com o suporte.</p>" +
                        "<p>Atenciosamente,<br>Equipe Kogni</p>" +
                        "</div>",
                nomeUsuario,
                nomeItemSolicitado,
                codigoItem,
                nomeItem != null ? nomeItem : "Não informado"
        );
    }

    public ItemSolicitationEntity updateTechnicalValidation(String id, TechnicalValidationSolicitationDTO dto, UUID userId) {
        ItemSolicitationEntity entity = findById(id);

        // Atualiza apenas os campos fornecidos (comportamento PATCH)
        if (dto.getMainName() != null) {
            entity.setMainName(dto.getMainName());
        }
        if (dto.getTipo() != null) {
            entity.setTipo(dto.getTipo());
        }
        if (dto.getUnidadeMedida() != null) {
            entity.setUnidadeMedida(dto.getUnidadeMedida());
        }
        if (dto.getItemGroup() != null) {
            entity.setItemGroup(dto.getItemGroup());
        }
        if (dto.getItemGroupName() != null) {
            entity.setItemGroupName(dto.getItemGroupName());
        }
        if (dto.getUrlReferency() != null) {
            entity.setUrlReferency(dto.getUrlReferency());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getItemCode() != null) {
            entity.setItemCode(dto.getItemCode());
        }
        if (dto.getItemName() != null) {
            entity.setItemName(dto.getItemName());
        }

        // Atualiza status para Validação Tributária e etapa para 3
        entity.setStatus(ItemSolicitationStatus.TAX_VALIDATION);
        entity.setEtapa(3);

        // Adiciona log de validação técnica (transição para validação tributária)
        StringBuilder logMessage = new StringBuilder("Solicitação atualizada na validação técnica e encaminhada para validação tributária.");
        if (dto.getItemName() != null) {
            logMessage.append(" Item: ").append(dto.getItemName());
        }
        if (dto.getItemCode() != null) {
            logMessage.append(" (Código: ").append(dto.getItemCode()).append(")");
        }
        if (dto.getTipo() != null) {
            logMessage.append(" Tipo: ").append(dto.getTipo());
        }

        StepLog technicalValidationLog = createStepLog(
                userId,
                "VALIDACAO_TECNICA",
                2,
                logMessage.toString()
        );
        addLog(entity, technicalValidationLog);

        return itemSolicitationRepository.save(entity);
    }

    public ItemSolicitationEntity updateTaxApproval(String id, TaxApprovalSolicitationDTO dto, UUID userId) {
        ItemSolicitationEntity entity = findById(id);

        log.debug("Aprovação tributária - DTO recebido: aplicacao={}, servico={}, ncm={}, definicaoFiscal={}, codigoSituacaoTributaria={}, codigoTratamentoIcms={}, codigoRegraPisCofins={}", 
                dto.getAplicacao(), dto.getServico(), dto.getNcm(), dto.getDefinicaoFiscal(), dto.getCodigoSituacaoTributaria(), dto.getCodigoTratamentoIcms(), dto.getCodigoRegraPisCofins());

        // Atualiza os campos de aprovação tributária
        if (dto.getAplicacao() != null && !dto.getAplicacao().isBlank()) {
            entity.setAplicacao(dto.getAplicacao());
            log.debug("Campo aplicacao atualizado: {}", dto.getAplicacao());
        }
        if (dto.getServico() != null && !dto.getServico().isBlank()) {
            entity.setServico(dto.getServico());
            log.debug("Campo servico atualizado: {}", dto.getServico());
        }
        if (dto.getNcm() != null && !dto.getNcm().isBlank()) {
            entity.setNcm(dto.getNcm());
            log.debug("Campo ncm atualizado: {}", dto.getNcm());
        }
        if (dto.getDefinicaoFiscal() != null && !dto.getDefinicaoFiscal().isBlank()) {
            entity.setDefinicaoFiscal(dto.getDefinicaoFiscal());
            log.debug("Campo definicaoFiscal atualizado: {}", dto.getDefinicaoFiscal());
        }
        if (dto.getCodigoSituacaoTributaria() != null && !dto.getCodigoSituacaoTributaria().isBlank()) {
            entity.setCodigoSituacaoTributaria(dto.getCodigoSituacaoTributaria());
            log.debug("Campo codigoSituacaoTributaria atualizado: {}", dto.getCodigoSituacaoTributaria());
        }
        if (dto.getCodigoTratamentoIcms() != null && !dto.getCodigoTratamentoIcms().isBlank()) {
            entity.setCodigoTratamentoIcms(dto.getCodigoTratamentoIcms());
            log.debug("Campo codigoTratamentoIcms atualizado: {}", dto.getCodigoTratamentoIcms());
        }
        if (dto.getCodigoRegraPisCofins() != null && !dto.getCodigoRegraPisCofins().isBlank()) {
            entity.setCodigoRegraPisCofins(dto.getCodigoRegraPisCofins());
            log.debug("Campo codigoRegraPisCofins atualizado: {}", dto.getCodigoRegraPisCofins());
        }

        // Atualiza status para "Cadastro" e etapa para 4
        entity.setStatus(ItemSolicitationStatus.REGISTRATION);
        entity.setEtapa(4);

        // Adiciona log de aprovação tributária
        StringBuilder logMessage = new StringBuilder("Aprovação tributária realizada.");
        if (dto.getAplicacao() != null) {
            logMessage.append(" Aplicação: ").append(dto.getAplicacao());
        }
        if (dto.getServico() != null) {
            logMessage.append(" Serviço: ").append(dto.getServico());
        }
        if (dto.getNcm() != null) {
            logMessage.append(" NCM: ").append(dto.getNcm());
        }
        if (dto.getDefinicaoFiscal() != null) {
            logMessage.append(" Definição Fiscal: ").append(dto.getDefinicaoFiscal());
        }
        if (dto.getCodigoSituacaoTributaria() != null) {
            logMessage.append(" Código Situação Tributária: ").append(dto.getCodigoSituacaoTributaria());
        }
        if (dto.getCodigoTratamentoIcms() != null) {
            logMessage.append(" Código Tratamento ICMS: ").append(dto.getCodigoTratamentoIcms());
        }
        if (dto.getCodigoRegraPisCofins() != null) {
            logMessage.append(" Código Regra PIS/COFINS: ").append(dto.getCodigoRegraPisCofins());
        }

        StepLog taxApprovalLog = createStepLog(
                userId,
                "APROVACAO_TRIBUTARIA",
                4,
                logMessage.toString()
        );
        addLog(entity, taxApprovalLog);

        return itemSolicitationRepository.save(entity);
    }

    public ItemSolicitationEntity updateGrupoItem(String id, UpdateGrupoItemSolicitationDTO dto, UUID userId) {
        ItemSolicitationEntity entity = findById(id);

        // Atualiza o campo grupoItem
        entity.setItemGroup(dto.getItemGroup());
        
        // Atualiza o nome do grupo se fornecido
        if (dto.getItemGroupName() != null) {
            entity.setItemGroupName(dto.getItemGroupName());
        }

        // Adiciona log de atualização
        StringBuilder logMessage = new StringBuilder("Grupo item atualizado para: " + dto.getItemGroup());
        if (dto.getItemGroupName() != null) {
            logMessage.append(" (Nome: ").append(dto.getItemGroupName()).append(")");
        }
        
        StepLog updateLog = createStepLog(
                userId,
                "ATUALIZACAO_GRUPO_ITEM",
                entity.getEtapa(),
                logMessage.toString()
        );
        addLog(entity, updateLog);

        return itemSolicitationRepository.save(entity);
    }

    private StepLog createStepLog(UUID userId, String actionName, int step, String observation) {
        return StepLog.builder()
                .id(UUID.randomUUID())
                .name(actionName)
                .created_at(new Date())
                .final_at(new Date())
                .user(userId)
                .step(step)
                .observation(observation)
                .group(null)
                .build();
    }

    private void addLog(ItemSolicitationEntity entity, StepLog log) {
        if (entity.getStepLog() == null) {
            entity.setStepLog(new ArrayList<>());
        }
        entity.getStepLog().add(log);
    }

    public List<ItemSolicitationEntity> findAllRegisteredItems() {
        return itemSolicitationRepository.findByStatus(ItemSolicitationStatus.REGISTERED);
    }

    public void adjustSequentialIds() {
        // Busca todas as solicitações
        List<ItemSolicitationEntity> all = itemSolicitationRepository.findAll();
        
        // Ordena por data de criação (manusear nulos com .now() para fallback ou primeiro)
        all.sort((a, b) -> {
            LocalDateTime dA = a.getCreationDate() != null ? a.getCreationDate() : LocalDateTime.MIN;
            LocalDateTime dB = b.getCreationDate() != null ? b.getCreationDate() : LocalDateTime.MIN;
            return dA.compareTo(dB);
        });
        
        long counter = 0;
        
        // Atualiza os sequenciais
        for (ItemSolicitationEntity item : all) {
            counter++;
            item.setSequentialId(counter);
        }
        
        // Salva todos
        itemSolicitationRepository.saveAll(all);
        
        // Atualiza a sequence no banco para o último valor
        sequenceGeneratorService.setSequence("item_solicitacao_sequence", counter);
        
        log.info("Ajuste de sequenciais concluído. Total de registros processados: {}", counter);
    }
}

