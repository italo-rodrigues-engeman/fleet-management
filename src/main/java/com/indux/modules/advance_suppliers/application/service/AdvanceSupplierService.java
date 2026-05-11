package com.indux.modules.advance_suppliers.application.service;

import com.indux.core.application.dto.generic.BatchStatusDTO;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.mapper.ModuleResponseMapper;
import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.modules.*;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.domain.repository.generic.RegionalRepository;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.domain.service.occurrence.AbstractOccurrenceService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.core.infra.notifcation.mailsender.CustomMailSender;
import com.indux.modules.advance_suppliers.domain.dto.AdvanceSuppliersFilter;
import com.indux.modules.advance_suppliers.domain.dto.AdvanceSuppliersRequest;
import com.indux.modules.advance_suppliers.domain.entities.AdvanceSuppliers;
import com.indux.modules.advance_suppliers.domain.entities.DatabaseSequenceAdvanceSupplier;
import com.indux.modules.advance_suppliers.domain.entities.OriginRequest;
import com.indux.modules.advance_suppliers.domain.entities.PendingNotification;
import com.indux.modules.advance_suppliers.domain.entities.log.AdvanceSupplierLog;
import com.indux.modules.advance_suppliers.domain.entities.log.IdCodeProjection;
import com.indux.modules.advance_suppliers.domain.repository.AdvanceSuppliersLogRepository;
import com.indux.modules.advance_suppliers.domain.repository.AdvanceSuppliersRepository;
import com.indux.modules.advance_suppliers.domain.repository.DatabaseSequenceAdvanceSuppliersRepository;
import com.indux.modules.advance_suppliers.domain.repository.PendingNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdvanceSupplierService extends AbstractOccurrenceService<AdvanceSuppliers> {
    private static final Logger log = LoggerFactory.getLogger(AdvanceSupplierService.class);
    
    private final AdvanceSuppliersRepository repository;
    private final AdvanceSuppliersLogRepository logRepository;
    private final DatabaseSequenceAdvanceSuppliersRepository sequenceRepository;
    private final ModuleManagementService moduleService;
    private final PendingNotificationRepository pendingNotificationRepository;
    private final ModuleResponseMapper moduleResponseMapper;
    private final RegionalRepository regionalRepository;
    @Value("${module.advance_suppliers.id}")
    private String modulo_id;

    protected AdvanceSupplierService(StorageService storageService, NotificationService notification, CustomMailSender mailSender, UserService userService, MongoTemplate mongo, AdvanceSuppliersRepository repository, AdvanceSuppliersLogRepository logRepository, DatabaseSequenceAdvanceSuppliersRepository sequenceRepository, ModuleManagementService moduleService, PendingNotificationRepository pendingNotificationRepository, ModuleResponseMapper moduleResponseMapper, RegionalRepository regionalRepository) {
        super(storageService, notification, mailSender, userService, mongo);
        this.repository = repository;
        this.logRepository = logRepository;
        this.sequenceRepository = sequenceRepository;
        this.moduleService = moduleService;
        this.pendingNotificationRepository = pendingNotificationRepository;
        this.moduleResponseMapper = moduleResponseMapper;
        this.regionalRepository = regionalRepository;
    }

    @Override
    public GenericMessage createOccurrence(Object dto, String userId) throws InterruptedException {
        var request = (AdvanceSuppliersRequest) dto;

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
        Employee applicant = userService.getCompleteEmployeeFromUser(UUID.fromString(userId));
        SimpleUser user = userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundEmployee("Solicitante não encontrado."));

        StepLog log = createStepLog(
                UUID.fromString(userId),
                module.getConfigEtapas().stream().filter(e -> e.getEtapa() == 1).findFirst().map(StepModule::getNome)
                        .orElse("Início da Solicitação"),
                1,
                request.motivo(),
                null
        );

        List<AttachmentEntity> attachments = null;
        if (request.anexos() != null) {
            attachments = createAttachment(request.anexos());
        }

        var sequenciaID = new DatabaseSequenceAdvanceSupplier();
        var sequence = sequenceRepository.save(sequenciaID);
        var occurrence = AdvanceSuppliers.fromFirstStep(request, attachments, applicant, log, sequence.getId(), user);

        var occurrenceSaved = saveOccurrence(occurrence);
        sequence.setDocumentId(occurrenceSaved.getId());
        sequenceRepository.save(sequence);
        //todo: verificar se a notificação está correta.
        createOccurrenceLog(log, occurrenceSaved.getId(), occurrenceSaved.getCodeID());
        notifyNextStep(module, request.idRegional(), request.origem() == OriginRequest.ORDER ? 2 : 1);
        return new GenericMessage("Pedido criada com sucesso", 201);
    }

    @Override
    protected AdvanceSuppliers getOccurrenceInternal(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Pedido não encontrada."));

    }

    @Override
    protected AdvanceSuppliers saveOccurrence(AdvanceSuppliers occurrence) {
        occurrence.setDataLog(new Date());
        setOrderFields(occurrence);
        
        return repository.save(occurrence);
    }

    @Override
    public GenericMessage moveToNextStep(String occurrenceId, Object dto, String userId) {
        var occurrence = getOccurrenceInternal(occurrenceId);
        var request = (AdvanceSuppliersRequest) dto;
        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        if (occurrence.getCurrentStep() == 0) {
            throw new ModuleFailure("A ocorrência já está finalizada.");
        } else if (occurrence.getCurrentStep() > 5) {
            throw new ModuleFailure("A ocorrência não pode avançar mais que isso.");
        }
        StepLog log = createStepLog(
                UUID.fromString(userId),
                module.getConfigEtapas().stream().filter(e -> e.getEtapa() == occurrence.getCurrentStep()).findFirst().map(StepModule::getNome)
                        .orElse("Avanço de etapa."),
                occurrence.getCurrentStep(),
                request.motivo(),
                null
        );
        AdvanceSuppliers changedOccurrence = null;
        if (occurrence.getCurrentStep() == 2) {
            if (request.aprovacaoGestor() == null)
                throw new ModuleFailure("Deve-se obter a aprovação do gestor para avançar de etapa.");
            changedOccurrence = AdvanceSuppliers.fromSecondStep(request, occurrence);
            notifyNextStep(module, occurrence.getRegionalId(), occurrence.getCurrentStep());
        }
        if (occurrence.getCurrentStep() == 3) {
            if (request.aprovacaoFinanceiro() == null || request.comprovante_pagamento() == null)
                throw new ModuleFailure("Aprovação financeira e comprovante de pagamento são necessários para avançar de etapa.");
            var attachment = createAttachment(List.of(request.comprovante_pagamento())).stream().findFirst().orElse(null);
            changedOccurrence = AdvanceSuppliers.fromThirdStep(request, attachment, occurrence);
            sendNotificationToApplicant(changedOccurrence, attachment);
            notifyNextStep(module, occurrence.getRegionalId(), occurrence.getCurrentStep());
            createInvoiceNotification(occurrence, getResponsibleStepFour(module, occurrence));
        }
        occurrence.setCurrentStep(occurrence.getCurrentStep() + 1);
        occurrence.getStepLog().add(log);
        saveOccurrence(occurrence);
        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());
        return new GenericMessage("Avançado para próxima etapa", 200);
    }

    public Set<UUID> getResponsibleStepFour(Modulo modulo, AdvanceSuppliers occurrence) {
        return modulo.getPermissoes().stream()
                .filter(p -> p.getStepsAllowed().contains(4))
                .filter(p -> p.getRegionais().contains(occurrence.getRegionalId()) ||
                        p.getRegionais().contains(0))
                .filter(p -> p.getProjetos().contains(occurrence.getProjectId()) || p.getProjetos().contains(0))
                .map(ModulePermission::getResponsable)
                .collect(Collectors.toSet());
    }

    public void createInvoiceNotification(AdvanceSuppliers occurrence, Set<UUID> responsibleUserIds) {
        if (responsibleUserIds.isEmpty()) {
            throw new ModuleFailure("Nenhum responsável encontrado para a etapa 4.");
        }

        PendingNotification task = PendingNotification.builder()
                .occurrenceId(occurrence.getId())
                .step(4)
                .dueDate(occurrence.getInvoiceDate().atStartOfDay().toInstant(ZoneOffset.UTC))
                .responsibleUserIds(
                        responsibleUserIds.stream().map(UUID::toString).collect(Collectors.toSet())
                )
                .type("INVOICE_SUBMISSION")
                .notified(false)
                .code(occurrence.getCodeID())
                .build();

        pendingNotificationRepository.save(task);
    }

    private void sendNotificationToApplicant(AdvanceSuppliers advanceSuppliers, AttachmentEntity attachment) {
        var notificationItem = new Notification();
        notificationItem.setType(NotificationType.EMAIL);
        notificationItem.setTo(advanceSuppliers.getApplicant());
        notificationItem.setSubject("Pedido nº" + advanceSuppliers.getCodeID() + " de adiantamento de fornecedores");
        notificationItem.setMessage("        O seu pedido de adiantamento à fornecedores foi pago.<br><br>\n" +
                "\n" +
                "        Seu pedido de adiantamento de número " + advanceSuppliers.getCodeID() + " foi pago! Em anexo está o comprovante de pagamento.<br><br>\n" +
                "\n" +
                "        <b>Agora, o seu pedido avançará para o envio da nota fiscal com o responsável da etapa.</b><br>\n");

        notificationItem.setAttachments(Map.of(
                attachment.getNome(), attachment.getFile()
        ));
        notificationItem.setTemplateName(null);

        notification.send(notificationItem);
    }

    @Override
    public GenericMessage rejectOccurrence(String occurrenceId, Object dto, String userId) {
        var occurrence = getOccurrenceInternal(occurrenceId);
        var request = (AdvanceSuppliersRequest) dto;
        checkStatus(occurrence);
        if (occurrence.getStatus() == DocumentStatus.APROVADO)
            throw new ModuleFailure("Não é possível rejeitar uma solicitação já finalizada.");

        occurrence.setStatus(DocumentStatus.REJEITADO);
        occurrence.setSituacao(DocumentStatus.FINALIZADO);
        occurrence.setCurrentStep(0);
        occurrence.setFinal_date(new Date());
        var log = createStepLog(UUID.fromString(userId),

                "Pedido Rejeitado", occurrence.getCurrentStep(), dto != null ? request.observacao() : "", null);
        addLog(occurrence, log);

        saveOccurrence(occurrence);
        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());
        notifyApplicant(occurrence.getStepLog().stream().findFirst().get(), "Sua solicitação foi negada.", "<br><br>\n"
                +
                "\n" +
                "        O seu pedido de Adiantamento à Fornecedores foi negado.<br><br>\n" +
                "\n" +
                "        Seu pedido de código " + occurrence.getCodeID() + " foi negado! Acesse o sistema para mais detalhes.<br><br>\n");
        return new GenericMessage("Solicitação rejeitada com sucesso.", 200);
    }

    private void createOccurrenceLog(StepLog log, String occurrenceID, Long code) {
        var occurrenceLog = new AdvanceSupplierLog(
                occurrenceID,
                code
        );

        occurrenceLog.setId(UUID.randomUUID());
        occurrenceLog.setCreated_at(log.getCreated_at());
        occurrenceLog.setUser(log.getUser());
        occurrenceLog.setName(log.getName());
        occurrenceLog.setGroup(log.getGroup());
        occurrenceLog.setObservation(log.getObservation());
        occurrenceLog.setFinal_at(log.getFinal_at());

        logRepository.save(occurrenceLog);
    }

    @Override
    public AdvanceSuppliers getOccurrence(String id) {
        return getOccurrenceInternal(id);
    }

    @Override
    public GenericMessage finalizeOccurrence(String occurrenceId, Object dto, String userId) {
        var request = (AdvanceSuppliersRequest) dto;
        var occurrence = getOccurrenceInternal(occurrenceId);
        checkStatus(occurrence);
        AttachmentEntity attachment = null;
        if (request.nota_fiscal() != null) {
            List<AttachmentEntity> attachments = createAttachment(List.of(request.nota_fiscal()));
            occurrence.setInvoiceDocument(attachments.stream().findFirst().orElse(null));
            attachment = attachments.stream().findFirst()
                    .orElseThrow(() -> new ModuleFailure("Erro ao criar anexo da nota fiscal."));
        }

        var changedOccurrence = AdvanceSuppliers.fromFourthStep(request, attachment, occurrence);
        changedOccurrence.setStatus(DocumentStatus.APROVADO);
        changedOccurrence.setSituacao(DocumentStatus.FINALIZADO);
        changedOccurrence.setCurrentStep(0);
        changedOccurrence.setFinal_date(new Date());
        var log = createStepLog(UUID.fromString(userId),
                "Finalização", 0, request.observacao(), null);
        addLog(changedOccurrence, log);

        saveOccurrence(changedOccurrence);
        sendLastNotificationToFinanceUser(changedOccurrence, getLastUserFinanceStep(changedOccurrence), attachment);
        createOccurrenceLog(log, changedOccurrence.getId(), changedOccurrence.getCodeID());
        notifyApplicant(changedOccurrence.getStepLog().stream().findFirst().get(), "Seu pedido foi finalizado.", "<br><br>\n" +
                "\n" +
                "        Seu pedido no módulo de Adiantamento à Fornecedores foi finalizada.<br><br>\n" +
                "\n" +
                "        Sua ocorrência de número " + occurrence.getCodeID() + " foi aprovada e finalizada.<br><br>\n");
        return new GenericMessage("Ocorrência finalizada com sucesso.", 200);
    }

    private UUID getLastUserFinanceStep(AdvanceSuppliers occurrence) {
        return occurrence.getStepLog().stream()
                .filter(log -> log.getStep() == 3)
                .map(StepLog::getUser)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new ModuleFailure("Não foi possível encontrar o usuário responsável pela etapa 3."));
    }

    private void sendLastNotificationToFinanceUser(AdvanceSuppliers advanceSuppliers, UUID user, AttachmentEntity attachment) {
        var financeResponsible = userService.getUserById(user.toString())
                .orElseThrow(() -> new ModuleFailure("Usuário responsável pela etapa 3 não encontrado."));
        var notificationItem = new Notification();
        notificationItem.setType(NotificationType.EMAIL);
        notificationItem.setTo(financeResponsible);
        notificationItem.setSubject("Pedido nº " + advanceSuppliers.getCodeID() + " de adiantamento de fornecedores");
        String messageContent;
        if (attachment != null) {
            messageContent = "        A nota fiscal do pedido de Adiantamento à Fornecedores foi adicionada!<br><br>\n" +
                    "\n" +
                    "        A nota fiscal do pedido de adiantamento de número " + advanceSuppliers.getCodeID() + " foi adicionada! Em anexo está a nota fiscal.<br><br>\n" +
                    "\n" + " Também foi gerado o Aviso de Recebimento: " + advanceSuppliers.getReceiptNotice() + "<br><br>\n" +
                    "        <b>Caso queira conferir mais informações, pode acessar o seu histórico!</b><br>\n";
        } else {
            messageContent = "        O pedido de Adiantamento à Fornecedores foi finalizado!<br><br>\n" +
                    "\n" +
                    "        O pedido de adiantamento de número " + advanceSuppliers.getCodeID() + " foi finalizado sem nota fiscal.<br><br>\n" +
                    "\n" + " Também foi gerado o Aviso de Recebimento: " + advanceSuppliers.getReceiptNotice() + "<br><br>\n" +
                    "        <b>Caso queira conferir mais informações, pode acessar o seu histórico!</b><br>\n";
        }
        notificationItem.setMessage(messageContent);

        // Only add attachment if it's not null
        if (attachment != null) {
            notificationItem.setAttachments(Map.of(
                    attachment.getNome(), attachment.getFile()
            ));
        }
        notificationItem.setTemplateName(null);

        notification.send(notificationItem);
    }

    @Override
    protected void updateOccurrenceAfterReview(AdvanceSuppliers occurrence, Object dto, String userId, boolean isEdit) {
        var request = (AdvanceSuppliersRequest) dto;

        StepLog correctionLog = StepLog.builder()
                .id(UUID.randomUUID())
                .name(isEdit ? "Edição das informações" : "Revisão finalizada")
                .group(null)
                .created_at(new Date())
                .step(occurrence.getCurrentStep())
                .user(UUID.fromString(userId))
                .final_at(new Date())
                .observation(request.observacao())
                .build();

        if (!isEdit) {
            // Reset das aprovações e volta para a etapa 2 ou 3 dependendo da origem
            occurrence.setStatus(DocumentStatus.ABERTO);
            occurrence.setManagerApprove(false);
            occurrence.setFinanceApprove(false);

            parcialUpdate(occurrence, request);

            notifyPreviousStep(
                    occurrence.getStepLog().getLast(),
                    occurrence.getCurrentStep() + 1
            );

            // Definir a etapa com base na origem
            if (occurrence.getOrigin() == OriginRequest.ORDER) {
                occurrence.setCurrentStep(3);
            } else {
                occurrence.setCurrentStep(2);
            }
        } else {
            // Quando é uma edição após revisão, não alteramos o status nem a etapa
            // O status e a etapa já foram definidos no método editAfterReview
            parcialUpdate(occurrence, request);
        }

        occurrence.getStepLog().add(correctionLog);
        saveOccurrence(occurrence);
    }

    private void parcialUpdate(AdvanceSuppliers occurrence, AdvanceSuppliersRequest request) {
        updateFieldIfNotNull(request.temCnpj(), occurrence::setIsCnpj);
        updateFieldIfNotNull(request.tipo_de_envio(), occurrence::setType);
        updateFieldIfNotNull(request.cnpj(), occurrence::setCnpj);
        updateFieldIfNotNull(request.cpf(), occurrence::setCpf);
        updateFieldIfNotNull(request.dadosBancarios(), occurrence::setDadosBancarios);
        updateFieldIfNotNull(request.chavePix(), occurrence::setPixKey);
        updateFieldIfNotNull(request.dataPagamento(), occurrence::setPaymentDate);
        updateFieldIfNotNull(request.dataPrevistaNotaFiscal(), occurrence::setInvoiceDate);
        updateFieldIfNotNull(request.origem(), occurrence::setOrigin);
        updateFieldIfNotNull(request.valor(), occurrence::setValue);
        updateFieldIfNotNull(request.nomeFornecedor(), occurrence::setSupplierName);
        updateFieldIfNotNull(request.motivo(), occurrence::setReason);
        updateFieldIfNotNull(request.gestorNome(), occurrence::setManagerName);
        updateFieldIfNotNull(request.gestorEmail(), occurrence::setManagerEmail);
        updateFieldIfNotNull(request.numeroContrato(), occurrence::setContractNumber);
        updateFieldIfNotNull(request.numeroPedido(), occurrence::setOrderNumber);
        updateFieldIfNotNull(request.idRegional(), occurrence::setRegionalId);
        updateFieldIfNotNull(request.pagamentoIntegral(), occurrence::setIntegralPayment);
        updateFieldIfNotNull(request.valorTeto(), occurrence::setCeilingValue);

        if (request.anexos() != null && !request.anexos().isEmpty()) {
            List<AttachmentEntity> newAttachments = createAttachment(request.anexos());
            List<AttachmentEntity> merged = mergeAttachments(occurrence.getAttachments(), newAttachments);
            occurrence.setAttachments(merged);
        }
    }

    private List<AttachmentEntity> mergeAttachments(List<AttachmentEntity> current, List<AttachmentEntity> incoming) {
        Map<String, AttachmentEntity> attachmentMap = new HashMap<>();

        if (current != null) {
            current.forEach(att -> attachmentMap.put(att.getNome(), att));
        }

        incoming.forEach(att -> attachmentMap.put(att.getNome(), att));

        return new ArrayList<>(attachmentMap.values());
    }
    @Override
    public GenericMessage requestReview(String occurrenceId, Object dto, String userId) {
        var occurrence = getOccurrenceInternal(occurrenceId);
        var request = (AdvanceSuppliersRequest) dto;
        checkStatus(occurrence);
        //A revisão SEMPRE mandará para a primeira etapa novamente.
        if (occurrence.getCurrentStep() <= 1) {
            throw new ModuleFailure("Não é possível voltar além da primeira etapa.");
        }

        occurrence.setStatus(DocumentStatus.REVISÃO);
        occurrence.setSituacao(DocumentStatus.ANDAMENTO);

        var log = createStepLog(UUID.fromString(userId),
                "Solicitação de Revisão", occurrence.getCurrentStep(), request.observacao(), null);
        addLog(occurrence, log);
        notifyPreviousStep(occurrence.getStepLog().getLast(), occurrence.getCurrentStep());
        occurrence.setCurrentStep(1);
        saveOccurrence(occurrence);
        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());

        return new GenericMessage("Revisão solicitada.", 200);
    }

    @Override
    public GenericMessage editAfterReview(String occurrenceId, Object dto, String userId, boolean isEdit) {
        try {
            var occurrence = getOccurrenceInternal(occurrenceId);
            var request = (AdvanceSuppliersRequest) dto;
            checkStatus(occurrence);
            
            StepLog log = createStepLog(
                    UUID.fromString(userId),
                    "Revisão concluída",
                    occurrence.getCurrentStep(),
                    request.observacao(),
                    null
            );
            
            // Atualizar os campos da ocorrência com os novos valores
            parcialUpdate(occurrence, request);
            
            // Definir o status como ABERTO conforme solicitado
            occurrence.setStatus(DocumentStatus.ABERTO);
            occurrence.setSituacao(DocumentStatus.ANDAMENTO);
            
            // Atualizar a etapa com base na origem da solicitação
            if (occurrence.getOrigin() == OriginRequest.ORDER) {
                // Se for pedido de compra, vai para etapa 3
                occurrence.setCurrentStep(3);
            } else {
                // Se não for pedido de compra, vai para etapa 2
                occurrence.setCurrentStep(2);
            }
            
            // Reset das aprovações
            occurrence.setManagerApprove(false);
            occurrence.setFinanceApprove(false);
            
            if (isEdit) {
                // Se for edição, não precisamos chamar updateOccurrenceAfterReview
                // pois já atualizamos os campos diretamente acima
                
                // Adicionar o log de edição
                StepLog correctionLog = StepLog.builder()
                        .id(UUID.randomUUID())
                        .name("Edição das informações")
                        .group(null)
                        .created_at(new Date())
                        .step(occurrence.getCurrentStep())
                        .user(UUID.fromString(userId))
                        .final_at(new Date())
                        .observation(request.observacao())
                        .build();
                
                occurrence.getStepLog().add(correctionLog);
            }
            
            addLog(occurrence, log);
            saveOccurrence(occurrence);
            createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());
            
            // Recarregar a ocorrência para garantir que temos os dados mais recentes
            AdvanceSuppliers updatedOccurrence = getOccurrenceInternal(occurrenceId);
            
            // Verificar se o status ainda é ABERTO e se a etapa está correta
            boolean needsUpdate = false;
            
            if (updatedOccurrence.getStatus() != DocumentStatus.ABERTO) {
                updatedOccurrence.setStatus(DocumentStatus.ABERTO);
                needsUpdate = true;
            }
            
            int correctStep = updatedOccurrence.getOrigin() == OriginRequest.ORDER ? 3 : 2;
            if (updatedOccurrence.getCurrentStep() != correctStep) {
                updatedOccurrence.setCurrentStep(correctStep);
                needsUpdate = true;
            }
            
            if (needsUpdate) {
                saveOccurrence(updatedOccurrence);
            }
            
            return new GenericMessage("Revisão concluída com sucesso. ID: " + updatedOccurrence.getId(), 200);
        } catch (Exception e) {
            return new GenericMessage("Erro ao processar revisão: " + e.getMessage(), 500);
        }
    }

    @Override
    public Page<AdvanceSuppliers> listOccurrencesByUser(UUID userId, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.asc("statusOrder"),
                        Sort.Order.asc("situacaoOrder")));

        return repository.findByApplicantId(userId, sortedPageable);
    }

    @Override
    public Page<AdvanceSuppliers> listOccurrencesAllowed(UUID userId, Pageable pageable) {
        Modulo modulo = moduleService.getModuleByID(UUID.fromString(modulo_id));
        ModuleResponseDTO module = moduleResponseMapper.toDTO(modulo, userId, false, false);

        Set<Integer> etapas = module.permissoesUsuario().etapasPermitidas();
        Set<Integer> regionaisPermitidas = module.permissoesUsuario().regionaisPermitidas();
        Set<Integer> projetosPermitidos = module.permissoesUsuario().projetosPermitidos();

        List<DocumentStatus> rejectStatus = List.of(
                DocumentStatus.REJEITADO,
                DocumentStatus.APROVADO);

        boolean allRegionais = regionaisPermitidas.contains(0);
        boolean allProjetos = projetosPermitidos.contains(0);

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.asc("statusOrder"),
                        Sort.Order.asc("situacaoOrder")));

        if (allRegionais && allProjetos) {
            return repository.findByCurrentStepInAndStatusNotIn(
                    etapas, rejectStatus, sortedPageable);
        }
        if (allRegionais) {
            return repository.findByCurrentStepInAndProjectIdInAndStatusNotIn(
                    etapas, projetosPermitidos, rejectStatus, sortedPageable);
        }
        if (allProjetos) {
            return repository.findByCurrentStepInAndRegionalIdInAndStatusNotIn(
                    etapas, regionaisPermitidas, rejectStatus, sortedPageable);
        }
        return repository.findByCurrentStepInAndRegionalIdInAndProjectIdInAndStatusNotIn(
                etapas, regionaisPermitidas, projetosPermitidos, rejectStatus, sortedPageable);
    }

    @Override
    public Page<AdvanceSuppliers> listAllAllowed(UUID userId, Pageable pageable) {
        Modulo modulo = moduleService.getModuleByID(UUID.fromString(modulo_id));
        ModuleResponseDTO module = moduleResponseMapper.toDTO(modulo, userId, false, false);

        Set<Integer> regionaisPermitidas = module.permissoesUsuario().regionaisPermitidas();
        Set<Integer> projetosPermitidos = module.permissoesUsuario().projetosPermitidos();

        boolean allRegionais = regionaisPermitidas.contains(0);
        boolean allProjetos = projetosPermitidos.contains(0);

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.asc("statusOrder"),
                        Sort.Order.asc("situacaoOrder")));

        if (allRegionais && allProjetos) {
            return repository.findAll(sortedPageable);
        }

        if (allRegionais) {
            return repository.findByProjectIdIn(projetosPermitidos, sortedPageable);
        }

        if (allProjetos) {
            return repository.findByRegionalIdIn(regionaisPermitidas, sortedPageable);
        }

        return repository.findByRegionalIdInAndProjectIdIn(regionaisPermitidas, projetosPermitidos, sortedPageable);
    }

    @Override
    public Page<AdvanceSuppliers> listAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<AdvanceSuppliers> searchFilter(Object filter, boolean filterGlobal, String userId, Pageable pageable) {
        var filterResolved = (AdvanceSuppliersFilter) filter;
        if (filterGlobal) return repository.findByFilter(filterResolved, pageable);
        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
        UUID user = UUID.fromString(userId);
        List<ModulePermission> userPerms = module.getPermissoes().stream()
                .filter(p -> user.equals(p.getResponsable()))
                .toList();
        List<Integer> branchAllowed = userPerms.stream()
                .flatMap(p -> p.getRegionais().stream())
                .distinct()
                .toList();
        List<Integer> projectsAllowed = userPerms.stream()
                .flatMap(p -> p.getProjetos().stream())
                .distinct()
                .toList();
        List<Integer> stepAllowed = userPerms.stream().flatMap(p -> p.getStepsAllowed().stream()).distinct().toList();
        var dto = filterResolved.withRegionalAndProject(branchAllowed, projectsAllowed, stepAllowed);
        return repository.findByFilter(dto, pageable);
    }

    @Override
    public void batchUpdateStatus(BatchStatusDTO ids, UUID user) {
        StepLog logger = createStepLog(
                user,
                "Rejeição em grupo",
                0,
                ids.observacao() != null ? ids.observacao()
                        : "Rejeição de " + ids.itens().size() + " itens sem observação.",
                null);
        List<IdCodeProjection> results = repository.findByIdIn(ids.itens());

        Map<String, Long> codeMap = results.stream()
                .collect(Collectors.toMap(IdCodeProjection::getId, IdCodeProjection::getCodeID));
        for (String id : ids.itens()) {
            Long codeID = codeMap.get(id);
            createOccurrenceLog(logger, id, codeID);
        }

        batchUpdateStatusGlobal(ids.itens(), logger, DocumentStatus.REJEITADO, AdvanceSuppliers.class);
    }

    private List<AttachmentEntity> createAttachment(List<AttachmentRecord> dtos) {
        List<AttachmentEntity> result = new ArrayList<>();
        for (AttachmentRecord dto : dtos) {
            // Pular elementos null na lista
            if (dto == null) {
                continue;
            }

            var item = new AttachmentEntity();
            item.setId(UUID.randomUUID().toString());
            item.setNome(dto.nome());
            String filename = item.getId() + " - " + LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                    .toString().replace(":", "-");
            FileMetadata store = storeFile(dto.file(), "adiantamento-fornecedores/anexos", filename, 1);
            item.setFile(store);
            result.add(item);
        }
        return result;
    }
}
