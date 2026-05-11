package com.indux.modules.purchase_occurrence.application;

import com.indux.core.application.dto.generic.BatchStatusDTO;
import com.indux.core.application.dto.generic.CompleteEmployeeDTO;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.dto.generic.GenericMessageWithCode;
import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.mapper.ModuleResponseMapper;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.modules.*;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.domain.service.occurrence.AbstractOccurrenceService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.core.infra.notifcation.mailsender.CustomMailSender;
import com.indux.modules.purchase_occurrence.domain.dto.PurchaseOccurrenceFilter;
import com.indux.modules.purchase_occurrence.domain.dto.PurchaseRequest;
import com.indux.modules.purchase_occurrence.domain.entities.DatabaseSequencePurchaseOccurrence;
import com.indux.modules.purchase_occurrence.domain.entities.PurchaseOccurrence;
import com.indux.modules.purchase_occurrence.domain.entities.log.IdCodeProjection;
import com.indux.modules.purchase_occurrence.domain.entities.log.PurchaseOccurrenceLog;
import com.indux.modules.purchase_occurrence.domain.repository.PurchaseOccurrenceCausesRepository;
import com.indux.modules.purchase_occurrence.domain.repository.PurchaseOccurrenceRepository;
import com.indux.modules.purchase_occurrence.domain.repository.SequencePurchaseOccurrenceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseOccurrenceService extends AbstractOccurrenceService<PurchaseOccurrence> {
    private final PurchaseOccurrenceRepository repository;
    private final SequencePurchaseOccurrenceRepository sequenceRepository;
    private final ModuleManagementService moduleService;
    private final PurchaseOccurrenceLogService logService;
    private final ModuleResponseMapper moduleResponseMapper;
    private final GetEmployeeUseCase emplyeeUseCase;
    @Value("${module.oc.id}")
    private String modulo_id;

    public PurchaseOccurrenceService(PurchaseOccurrenceRepository repository, PurchaseOccurrenceCausesRepository causesRepository, SequencePurchaseOccurrenceRepository sequence, StorageService storageService, NotificationService notification, CustomMailSender mailSender, UserService userService, MongoTemplate mongo, ModuleManagementService moduleService, PurchaseOccurrenceLogService logService, ModuleResponseMapper moduleResponseMapper, GetEmployeeUseCase emplyeeUseCase) {
        super(storageService, notification, mailSender, userService, mongo);
        this.repository = repository;
        this.sequenceRepository = sequence;
        this.moduleService = moduleService;
        this.logService = logService;
        this.moduleResponseMapper = moduleResponseMapper;
        this.emplyeeUseCase = emplyeeUseCase;
    }

    @Override
    public GenericMessage createOccurrence(Object dto, String userId) throws InterruptedException {
        GenericMessageWithCode result = createPurchaseOccurrence(dto, userId);
        return new GenericMessage(result.message(), result.status());
    }

    public GenericMessageWithCode createPurchaseOccurrence(Object dto, String userId) throws InterruptedException {
        PurchaseRequest request = (PurchaseRequest) dto;

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        Employee applicant = userService.getCompleteEmployeeFromUser(UUID.fromString(userId));
        SimpleUser user = userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundEmployee("Solicitante não encontrado."));


        StepLog log = createStepLog(
                UUID.fromString(userId),
                module.getConfigEtapas().stream().filter(e -> e.getEtapa() == 1).findFirst().map(StepModule::getNome)
                        .orElse("Início da Solicitação"),
                1,
                request.justificativaSolicitacao(),
                null
        );

        var attachments = createAttachment(((PurchaseRequest) dto).anexos());
        var sequenciaID = new DatabaseSequencePurchaseOccurrence();
        DatabaseSequencePurchaseOccurrence sequence = sequenceRepository.save(sequenciaID);
        CompleteEmployeeDTO employee = new CompleteEmployeeDTO();
        if(((PurchaseRequest) dto).comprador().id() != null){
            employee = emplyeeUseCase.getEmployeeById(((PurchaseRequest) dto).comprador().id());
        }
        var occurrence = PurchaseOccurrence.initialStep((PurchaseRequest) dto, applicant, log, sequence.getId(), attachments, user,employee);
        PurchaseOccurrence occurrenceSaved = saveOccurrence(occurrence);
        sequence.setDocumentId(occurrenceSaved.getId());
        sequenceRepository.save(sequence);
        //todo: verificar se a notificação está correta.
        createOccurrenceLog(log, occurrenceSaved.getId(), occurrenceSaved.getCodeID());
        
        // Exemplo de uso dos métodos melhorados - incluindo código da ocorrência e nome do módulo
        String occurrenceCode = String.valueOf(occurrenceSaved.getCodeID());
        String moduleName = "Ocorrência de Compra";
        var regionalId = employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                ? employee.getHierarchy().getRegionalId().intValue() : null;
        notifyNextStep(module, regionalId, 1, occurrenceCode, moduleName);
        
        return new GenericMessageWithCode("Ocorrência criada com sucesso", 201, occurrenceSaved.getCodeID());
    }

    @Override
    public GenericMessage moveToNextStep(String occurrenceId, Object dto, String userId) {
        return null;
    }

    private List<AttachmentEntity> createAttachment(List<AttachmentRecord> dtos) {
        List<AttachmentEntity> result = new ArrayList<>();
        for (AttachmentRecord dto : dtos) {
            var item = new AttachmentEntity();
            item.setId(UUID.randomUUID().toString());
            String filename = item.getId() + " - " + LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                    .toString().replace(":", "-");
            item.setNome(dto.nome());
            FileMetadata store = storeFile(dto.file(), "ocorrencia-compra/anexos", filename, 1);
            item.setFile(store);
            result.add(item);
        }
        return result;
    }

    @Override
    protected PurchaseOccurrence getOccurrenceInternal(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Ocorrência não encontrada."));
    }

    @Override
    protected void updateOccurrenceAfterReview(PurchaseOccurrence occurrence, Object dto, String userId, boolean isEdit) {
        var request = (PurchaseRequest) dto;

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

        if(!isEdit) {
            occurrence.setStatus(DocumentStatus.ABERTO);
            parcialUpdate(occurrence, (PurchaseRequest) dto);
            
            // Exemplo de uso do método melhorado notifyPreviousStep
            String occurrenceCode = String.valueOf(occurrence.getCodeID());
            String moduleName = "Ocorrência de Compra";
            notifyPreviousStep(occurrence.getStepLog().getLast(), occurrence.getCurrentStep() + 1, occurrenceCode, moduleName);
            
            occurrence.setCurrentStep(2);
        }
        parcialUpdate(occurrence, (PurchaseRequest) dto);
        occurrence.getStepLog().add(correctionLog);
        saveOccurrence(occurrence);
    }

    private void parcialUpdate(PurchaseOccurrence purchaseOccurrence, PurchaseRequest updateRecord) {
        updateFieldIfNotNull(updateRecord.numeroDoPedido(), purchaseOccurrence::setOrderNumber);
        updateFieldIfNotNull(updateRecord.numeroNotaFiscal(), purchaseOccurrence::setInvoiceNumber);
        updateFieldIfNotNull(updateRecord.causas(), purchaseOccurrence::setCauses);
        updateFieldIfNotNull(updateRecord.justificativaSolicitacao(), purchaseOccurrence::setJustificationFirstStep);
        updateFieldIfNotNull(updateRecord.comprador(), purchaseOccurrence::setBuyer);
        updateFieldIfNotNull(updateRecord.cnpjDivergente(), purchaseOccurrence::setDivergenceCnpj);
        updateFieldIfNotNull(updateRecord.nomeDivergente(), purchaseOccurrence::setDivergenceName);
        updateFieldIfNotNull(updateRecord.outros(), purchaseOccurrence::setOutros);

        if (updateRecord.anexosRemover() != null) {
            Set<String> toRemove = new HashSet<>(updateRecord.anexosRemover());
            List<AttachmentEntity> toRemoveEntity = purchaseOccurrence.getArchives().stream().filter(e -> toRemove.contains(e.getId())).toList();
            purchaseOccurrence.getArchives()
                    .removeAll(toRemoveEntity);
            if (!toRemoveEntity.isEmpty()) {
                for(var item : toRemoveEntity) {
                    try {
                        storageService.deleteFile(Path.of(storageService.getRootLocation() + "/" + item.getFile().getPath()));
                    } catch (Exception e) {
                       throw new ModuleFailure("Erro ao remover arquivo: " + item.getFile().getPath() + " " +  e);
                    }
                }
            }

        }
        if (updateRecord.anexos() != null && !updateRecord.anexos().isEmpty()) {
            List<AttachmentEntity> newAttachments = createAttachment(updateRecord.anexos());
            List<AttachmentEntity> merged = mergeAttachments(purchaseOccurrence.getArchives(), newAttachments);
            purchaseOccurrence.setArchives(merged);
        }
    }

    private List<AttachmentEntity> mergeAttachments(List<AttachmentEntity> current, List<AttachmentEntity> incoming) {
        Map<String, AttachmentEntity> map = new LinkedHashMap<>();
        current.forEach(att -> map.put(att.getId(), att));
        incoming.forEach(att -> map.put(att.getId(), att));
        return new ArrayList<>(map.values());
    }

    @Override
    public Page<PurchaseOccurrence> searchFilter(Object filter, boolean filterGlobal, String userId, Pageable pageable) {
        var filterResolved = (PurchaseOccurrenceFilter) filter;
        if (filterGlobal) return repository.findByFilter(filterResolved, pageable);
        
        // Se o filtro por solicitante ou ID estiver ativo, permite acesso global para ver todas as ocorrências
        if (filterResolved.solicitante() != null || filterResolved.id() != null) {
            return repository.findByFilter(filterResolved, pageable);
        }
        
        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
        UUID user = UUID.fromString(userId);
        List<ModulePermission> userPerms = module.getPermissoes().stream()
                .filter(p -> user.equals(p.getResponsable()))
                .toList();
        List<Integer> regionaisAllowed = userPerms.stream()
                .flatMap(p -> p.getRegionais().stream())
                .distinct()
                .toList();
        List<Integer> projectsAllowed = userPerms.stream()
                .flatMap(p -> p.getProjetos().stream())
                .distinct()
                .toList();
        List<Integer> stepAllowed = userPerms.stream().flatMap(p -> p.getStepsAllowed().stream()).distinct().toList();
        PurchaseOccurrenceFilter dto = filterResolved.withRegionaisAndProjects(regionaisAllowed, projectsAllowed, stepAllowed);
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

        batchUpdateStatusGlobal(ids.itens(), logger, DocumentStatus.REJEITADO, PurchaseOccurrence.class);
    }

    @Override
    public GenericMessage requestReview(String occurrenceId, Object dto, String userId) {
        PurchaseOccurrence occurrence = getOccurrenceInternal(occurrenceId);
        checkStatus(occurrence);
        if (occurrence.getCurrentStep() <= 1) {
            throw new ModuleFailure("Não é possível voltar além da primeira etapa.");
        }

        occurrence.setStatus(DocumentStatus.REVISÃO);
        occurrence.setSituacao(DocumentStatus.ANDAMENTO);
        //Todo: pegar a justificativa geral.
        var log = createStepLog(UUID.fromString(userId),
                "Solicitação de Revisão", occurrence.getCurrentStep(), ((PurchaseRequest) dto).observacao(), null);
        addLog(occurrence, log);
        notifyPreviousStep(occurrence.getStepLog().getLast(), occurrence.getCurrentStep());
        occurrence.setCurrentStep(occurrence.getCurrentStep() - 1);
        saveOccurrence(occurrence);
        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());

        return new GenericMessage("Revisão solicitada.", 200);
    }

    @Override
    public GenericMessage editAfterReview(String occurrenceId, Object dto, String userId, boolean isEdit) {
        PurchaseOccurrence occurrence = getOccurrenceInternal(occurrenceId);

        if (occurrence == null) {
            throw new ModuleNotFoundFailure("Ocorrência não encontrada.");
        }

        if (!isEdit && !DocumentStatus.REVISÃO.equals(occurrence.getStatus())) {
            throw new ModuleFailure(
                    "Só é possível editar ocorrências que estão em revisão.");
        }

        updateOccurrenceAfterReview(occurrence, dto, userId, isEdit);

        saveOccurrence(occurrence);

        return new GenericMessage("Ocorrência editada com sucesso após revisão.", 200);
    }

    @Override
    public GenericMessage rejectOccurrence(String occurrenceId, Object dto, String userId) {
        PurchaseOccurrence occurrence = getOccurrenceInternal(occurrenceId);
        checkStatus(occurrence);
        if (occurrence.getStatus() == DocumentStatus.APROVADO)
            throw new ModuleFailure("Não é possível rejeitar uma solicitação já finalizada.");

        occurrence.setStatus(DocumentStatus.REJEITADO);
        occurrence.setSituacao(DocumentStatus.FINALIZADO);
        occurrence.setCurrentStep(0);
        occurrence.setFinal_date(Date.from(Instant.now()));
        var log = createStepLog(UUID.fromString(userId),

                "Ocorrência Rejeitada", occurrence.getCurrentStep(), dto != null ? ((PurchaseRequest) dto).observacao() : "", null);
        addLog(occurrence, log);

        saveOccurrence(occurrence);
        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());
        
        // Exemplo de uso do método melhorado notifyApplicant
        String occurrenceCode = String.valueOf(occurrence.getCodeID());
        String moduleName = "Ocorrência de Compra";
        notifyApplicant(occurrence.getStepLog().stream().findFirst().get(), 
                "Sua solicitação foi negada.", 
                "<br><br>\n" +
                "\n" +
                "        A sua Ocorrência de Folha Financeira foi negada.<br><br>\n" +
                "\n" +
                "        Sua ocorrência foi negada! Acesse o sistema para mais detalhes.<br><br>\n",
                occurrenceCode, 
                moduleName);
        
        return new GenericMessage("Ocorrência rejeitada com sucesso.", 200);

    }

    @Override
    public GenericMessage finalizeOccurrence(String occurrenceId, Object dto, String userId) {
        var request = (PurchaseRequest) dto;
        PurchaseOccurrence occurrence = getOccurrenceInternal(occurrenceId);
        checkStatus(occurrence);
        FileMetadata metadata = null;
        if(request.anexo() != null) {
            metadata = storeFile(request.anexo(), "ocorrencia-compra/anexos/suprimentos",
                    occurrenceId + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 2);

        }

        var changedOccurence = PurchaseOccurrence.secondStep(occurrence, request, metadata);
        changedOccurence.setStatus(DocumentStatus.APROVADO);
        changedOccurence.setSituacao(DocumentStatus.FINALIZADO);
        changedOccurence.setCurrentStep(0);
        ;
        changedOccurence.setFinal_date(new Date());
        var log = createStepLog(UUID.fromString(userId),
                "Finalização", 0, ((PurchaseRequest) dto).respostaSuprimentos(), null);
        addLog(changedOccurence, log);

        saveOccurrence(changedOccurence);
        createOccurrenceLog(log, changedOccurence.getId(), changedOccurence.getCodeID());
        
        // Exemplo de uso do método melhorado notifyApplicant
        String occurrenceCode = String.valueOf(occurrence.getCodeID());
        String moduleName = "Ocorrência de Compra";
        notifyApplicant(changedOccurence.getStepLog().stream().findFirst().get(), 
                "Sua solicitação foi finalizada.", 
                "<br><br>\n" +
                "\n" +
                "        Sua Ocorrência de Pedido de Compra foi finalizada.<br><br>\n" +
                "\n" +
                "        Sua ocorrência foi aprovada e finalizada.<br><br>\n",
                occurrenceCode, 
                moduleName);
        
        return new GenericMessage("Ocorrência finalizada com sucesso.", 200);
    }

    @Override
    public PurchaseOccurrence getOccurrence(String id) {
        return getOccurrenceInternal(id);
    }

    @Override
    protected PurchaseOccurrence saveOccurrence(PurchaseOccurrence occurrence) {
        occurrence.setDataLog(Date.from(Instant.now()));
        setOrderFields(occurrence);
        return repository.save(occurrence);
    }

    //ABERTOS POR MIM
    @Override
    public Page<PurchaseOccurrence> listOccurrencesByUser(UUID userId, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.asc("statusOrder"),
                        Sort.Order.asc("situacaoOrder")));

        return repository.findByApplicantId(userId, sortedPageable);
    }


    //PENDENTES PARA O USUÁRIO
    @Override
    public Page<PurchaseOccurrence> listOccurrencesAllowed(UUID userId, Pageable pageable) {
        Modulo modulo = moduleService.getModuleByID(UUID.fromString(modulo_id));
        ModuleResponseDTO module = moduleResponseMapper.toDTO(modulo, userId, false, false);

        Set<Integer> etapas = module.permissoesUsuario().etapasPermitidas();
        Set<Integer> regionais = module.permissoesUsuario().regionaisPermitidas();
        Set<Integer> projetos = module.permissoesUsuario().projetosPermitidos();

        List<DocumentStatus> rejectStatus = List.of(
                DocumentStatus.REJEITADO,
                DocumentStatus.APROVADO);

        boolean allRegionais = regionais.contains(0);
        boolean allProjects = projetos.contains(0);

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.asc("statusOrder"),
                        Sort.Order.asc("situacaoOrder")));

        if (allRegionais && allProjects) {
            return repository.findByCurrentStepInAndStatusNotIn(
                    etapas, rejectStatus, sortedPageable);
        }
        if (allRegionais) {
            return repository.findByCurrentStepInAndProjectIdInAndStatusNotIn(
                    etapas, projetos, rejectStatus, sortedPageable);
        }
        if (allProjects) {
            return repository.findByCurrentStepInAndRegionalIdInAndStatusNotIn(
                    etapas, regionais, rejectStatus, sortedPageable);
        }
        return repository.findByCurrentStepInAndRegionalIdInAndProjectIdInAndStatusNotIn(
                etapas, regionais, projetos, rejectStatus, sortedPageable);
    }

    //TODAS OCORRÊNCIAS PERMITIDAS INDIFERENTE DO STATUS.
    @Override
    public Page<PurchaseOccurrence> listAllAllowed(UUID userId, Pageable pageable) {
        Modulo modulo = moduleService.getModuleByID(UUID.fromString(modulo_id));
        ModuleResponseDTO module = moduleResponseMapper.toDTO(modulo, userId, false, false);

        Set<Integer> regionais = module.permissoesUsuario().regionaisPermitidas();
        Set<Integer> projetos = module.permissoesUsuario().projetosPermitidos();

        boolean allRegionais = regionais.contains(0);
        boolean allProjects = projetos.contains(0);

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.asc("statusOrder"),
                        Sort.Order.asc("situacaoOrder")));

        if (allRegionais && allProjects) {
            return repository.findAll(sortedPageable);
        }

        if (allRegionais) {
            return repository.findByProjectIdIn(projetos, sortedPageable);
        }

        if (allProjects) {
            return repository.findByRegionalIdIn(regionais, sortedPageable);
        }

        return repository.findByRegionalIdInAndProjectIdIn(regionais, projetos, sortedPageable);
    }

    @Override
    public Page<PurchaseOccurrence> listAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    private void createOccurrenceLog(StepLog log, String occurrenceID, Long code) {
        var occurrenceLog = new PurchaseOccurrenceLog(
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

        logService.save(occurrenceLog);
    }


}
