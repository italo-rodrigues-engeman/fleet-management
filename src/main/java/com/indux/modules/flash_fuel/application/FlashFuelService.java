package com.indux.modules.flash_fuel.application;

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
import com.indux.core.domain.repository.generic.RegionalRepository;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.domain.service.occurrence.AbstractOccurrenceService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.core.infra.notifcation.mailsender.CustomMailSender;
import com.indux.modules.flash_fuel.domain.dtos.FlashFuelFilter;
import com.indux.modules.flash_fuel.domain.dtos.FlashFuelRequest;
import com.indux.modules.flash_fuel.domain.entities.DatabaseSequenceFlashFuel;
import com.indux.modules.flash_fuel.domain.entities.FlashFuel;
import com.indux.modules.flash_fuel.domain.entities.log.FlashFuelLog;
import com.indux.modules.flash_fuel.domain.entities.log.IdCodeProjection;
import com.indux.modules.flash_fuel.domain.repository.DatabaseSequenceFlashFuelRepository;
import com.indux.modules.flash_fuel.domain.repository.FlashFuelLogRepository;
import com.indux.modules.flash_fuel.domain.repository.FlashFuelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlashFuelService extends AbstractOccurrenceService<FlashFuel> {
    private static final Logger log = LoggerFactory.getLogger(FlashFuelService.class);

    private final FlashFuelRepository repository;
    private final FlashFuelLogRepository logRepository;
    private final DatabaseSequenceFlashFuelRepository sequenceRepository;
    private final ModuleManagementService moduleService;
    private final ModuleResponseMapper moduleResponseMapper;
    private final RegionalRepository regionalRepository;
    @Value("${module.flashFuel.id}")
    private String modulo_id;

    protected FlashFuelService(StorageService storageService, NotificationService notification,
            CustomMailSender mailSender, UserService userService, MongoTemplate mongo, FlashFuelRepository repository,
            FlashFuelLogRepository logRepository, DatabaseSequenceFlashFuelRepository sequenceRepository,
            ModuleManagementService moduleService, ModuleResponseMapper moduleResponseMapper,
            RegionalRepository regionalRepository) {
        super(storageService, notification, mailSender, userService, mongo);
        this.repository = repository;
        this.logRepository = logRepository;
        this.sequenceRepository = sequenceRepository;
        this.moduleService = moduleService;
        this.moduleResponseMapper = moduleResponseMapper;
        this.regionalRepository = regionalRepository;
    }

    @Override
    protected void updateOccurrenceAfterReview(FlashFuel occurrence, Object dto, String userId, boolean isEdit) {
        var request = (FlashFuelRequest) dto;
        StepLog correctionLog = StepLog.builder().id(UUID.randomUUID())
                .name(isEdit ? "Edição das informações" : "Revisão finalizada").group(null).created_at(new Date())
                .step(occurrence.getCurrentStep()).user(UUID.fromString(userId)).final_at(new Date())
                .observation(request.observacao()).build();

        if (!isEdit) {
            occurrence.setStatus(DocumentStatus.ABERTO);
            occurrence.setManagerApproval(false);
            occurrence.setFinanceApproval(false);
            parcialUpdate(occurrence, request);

            Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
            String occurrenceCode = String.valueOf(occurrence.getCodeID());
            String moduleName = module.getName();
            notifyPreviousStep(occurrence.getStepLog().getLast(), occurrence.getCurrentStep() + 1, occurrenceCode,
                    moduleName);

            occurrence.setCurrentStep(2);
        }
        parcialUpdate(occurrence, request);
        occurrence.getStepLog().add(correctionLog);
        saveOccurrence(occurrence);
    }

    private void parcialUpdate(FlashFuel flashFuelEntity, FlashFuelRequest updateRecord) {
        updateFieldIfNotNull(updateRecord.colaborador(), flashFuelEntity::setIsEmployee);
        updateFieldIfNotNull(updateRecord.dataSolicitacao(), flashFuelEntity::setDate);
        updateFieldIfNotNull(updateRecord.matriculaSolicitante(), flashFuelEntity::setRegistration);
        updateFieldIfNotNull(updateRecord.nomeSolicitante(), flashFuelEntity::setClaimantName);
        updateFieldIfNotNull(updateRecord.cpfSolicitante(), flashFuelEntity::setClaimantCPF);
        updateFieldIfNotNull(updateRecord.gestorFilial(), flashFuelEntity::setGestorFilial);
        updateFieldIfNotNull(updateRecord.getValorAsBigDecimal(), flashFuelEntity::setValue);
        updateFieldIfNotNull(updateRecord.motivo(), flashFuelEntity::setMotivo);
        updateFieldIfNotNull(updateRecord.getValorContratoAsList(), flashFuelEntity::setValorContrato);

        if (updateRecord.anexosRemover() != null) {
            Set<String> toRemove = new HashSet<>(updateRecord.anexosRemover());
            List<AttachmentEntity> toRemoveEntity = flashFuelEntity.getAttachments().stream()
                    .filter(e -> toRemove.contains(e.getId())).toList();
            flashFuelEntity.getAttachments().removeAll(toRemoveEntity);
            toRemoveEntity.forEach(item -> {
                try {
                    storageService
                            .deleteFile(Path.of(storageService.getRootLocation() + "/" + item.getFile().getPath()));
                } catch (Exception e) {
                    throw new ModuleFailure("Erro ao remover arquivo: " + item.getFile().getPath());
                }
            });

            if (updateRecord.anexos() != null && !updateRecord.anexos().isEmpty()) {
                List<AttachmentEntity> newAttachments = createAttachment(updateRecord.anexos());
                List<AttachmentEntity> merged = mergeAttachments(flashFuelEntity.getAttachments(), newAttachments);
                flashFuelEntity.setAttachments(merged);
            }

        }
    }

    private List<AttachmentEntity> mergeAttachments(List<AttachmentEntity> current, List<AttachmentEntity> incoming) {
        Map<String, AttachmentEntity> map = new LinkedHashMap<>();
        current.forEach(att -> map.put(att.getId(), att));
        incoming.forEach(att -> map.put(att.getId(), att));
        return new ArrayList<>(map.values());
    }

    @Override
    public GenericMessage requestReview(String occurrenceId, Object dto, String userId) {
        var occurrence = getOccurrenceInternal(occurrenceId);
        var request = (FlashFuelRequest) dto;
        checkStatus(occurrence);
        // A revisão SEMPRE mandará para a primeira etapa novamente.
        if (occurrence.getCurrentStep() <= 1) {
            throw new ModuleFailure("Não é possível voltar além da primeira etapa.");
        }

        occurrence.setStatus(DocumentStatus.REVISÃO);
        occurrence.setSituacao(DocumentStatus.ANDAMENTO);

        var log = createStepLog(UUID.fromString(userId), "Solicitação de Revisão", occurrence.getCurrentStep(),
                request.observacao(), null);
        addLog(occurrence, log);

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
        String occurrenceCode = String.valueOf(occurrence.getCodeID());
        String moduleName = module.getName();
        notifyPreviousStep(occurrence.getStepLog().getLast(), occurrence.getCurrentStep(), occurrenceCode, moduleName);

        occurrence.setCurrentStep(1);
        saveOccurrence(occurrence);
        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());

        return new GenericMessage("Revisão solicitada.", 200);
    }

    @Override
    public GenericMessage editAfterReview(String occurrenceId, Object dto, String userId, boolean isEdit) {
        var occurrence = getOccurrenceInternal(occurrenceId);

        if (occurrence == null) {
            throw new ModuleNotFoundFailure("Ocorrência não encontrada.");
        }

        if (!isEdit && !DocumentStatus.REVISÃO.equals(occurrence.getStatus())) {
            throw new ModuleFailure("Só é possível editar ocorrências que estão em revisão.");
        }

        updateOccurrenceAfterReview(occurrence, dto, userId, isEdit);

        saveOccurrence(occurrence);

        return new GenericMessage("Ocorrência editada com sucesso após revisão.", 200);
    }

    @Override
    public Page<FlashFuel> searchFilter(Object filter, boolean filterGlobal, String userId, Pageable pageable) {
        var filterResolved = (FlashFuelFilter) filter;
        if (filterGlobal)
            return repository.findByFilter(filterResolved, pageable);
        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
        UUID user = UUID.fromString(userId);
        List<ModulePermission> userPerms = module.getPermissoes().stream().filter(p -> user.equals(p.getResponsable()))
                .toList();
        List<Integer> regionalAllowed = userPerms.stream().flatMap(p -> p.getRegionais().stream()).distinct().toList();
        List<Integer> projectsAllowed = userPerms.stream().flatMap(p -> p.getProjetos().stream()).distinct().toList();
        List<Integer> stepAllowed = userPerms.stream().flatMap(p -> p.getStepsAllowed().stream()).distinct().toList();
        var dto = filterResolved.withRegionaisAndProjetcs(regionalAllowed, projectsAllowed, stepAllowed);
        return repository.findByFilter(dto, pageable);
    }

    @Override
    protected FlashFuel getOccurrenceInternal(String id) {
        return repository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Ocorrência não encontrada."));
    }

    @Override
    protected FlashFuel saveOccurrence(FlashFuel occurrence) {
        occurrence.setDataLog(Date.from(Instant.now()));
        setOrderFields(occurrence);

        return repository.save(occurrence);
    }

    @Override
    public GenericMessage createOccurrence(Object dto, String userId) throws InterruptedException {
        var request = (FlashFuelRequest) dto;

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        Employee applicant = userService.getCompleteEmployeeFromUser(UUID.fromString(userId));
        SimpleUser user = userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundEmployee("Solicitante não encontrado."));

        StepLog log = createStepLog(UUID.fromString(userId), module.getConfigEtapas().stream()
                .filter(e -> e.getEtapa() == 1).findFirst().map(StepModule::getNome).orElse("Início da Solicitação"), 1,
                request.motivo(), null);

        var attachments = createAttachment(request.anexos());
        var sequenciaID = new DatabaseSequenceFlashFuel();
        DatabaseSequenceFlashFuel sequence = sequenceRepository.save(sequenciaID);
        var occurrence = FlashFuel.fromFirstStep(request, attachments, log, sequence.getId(), user);

        FlashFuel occurrenceSaved = saveOccurrence(occurrence);
        sequence.setDocumentId(occurrenceSaved.getId());
        sequenceRepository.save(sequence);
        // todo: verificar se a notificação está correta.
        createOccurrenceLog(log, occurrenceSaved.getId(), occurrenceSaved.getCodeID());

        String occurrenceCode = String.valueOf(occurrenceSaved.getCodeID());
        String moduleName = module.getName();
        notifyNextStep(module, request.regionalId(), 1, occurrenceCode, moduleName);

        return new GenericMessage("Pedido criada com sucesso", 201);
    }

    @Override
    public GenericMessage moveToNextStep(String occurrenceId, Object dto, String userId) {
        var occurrence = getOccurrenceInternal(occurrenceId);
        var request = (FlashFuelRequest) dto;
        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        if (occurrence.getCurrentStep() == 0) {
            throw new ModuleFailure("A ocorrência já está finalizada.");
        } else if (occurrence.getCurrentStep() > 4) {
            throw new ModuleFailure("A ocorrência não pode avançar mais que isso.");
        }

        StepLog log = createStepLog(UUID.fromString(userId),
                module.getConfigEtapas().stream().filter(e -> e.getEtapa() == occurrence.getCurrentStep()).findFirst()
                        .map(StepModule::getNome).orElse("Avanço de etapa."),
                occurrence.getCurrentStep(), request.motivo(), null);

        String occurrenceCode = String.valueOf(occurrence.getCodeID());
        String moduleName = module.getName();

        FlashFuel changedOccurrence = null;
        if (occurrence.getCurrentStep() == 2) {
            if (request.aprovacaoGestor() == null)
                throw new ModuleFailure("Deve-se obter a aprovação do gestor para avançar de etapa.");
            changedOccurrence = FlashFuel.fromSecondStep(occurrence, request);
            notifyNextStep(module, occurrence.getRegionalId(), occurrence.getCurrentStep(), occurrenceCode, moduleName);
        } else if (occurrence.getCurrentStep() == 3) {
            if (request.aprovacaoFinanceiro() == null)
                throw new ModuleFailure("Deve-se obter a aprovação do financeiro para avançar de etapa.");
            changedOccurrence = FlashFuel.fromThirdStep(occurrence, request);
            notifyNextStep(module, occurrence.getRegionalId(), occurrence.getCurrentStep(), occurrenceCode, moduleName);
        } else if (occurrence.getCurrentStep() == 4) {
            changedOccurrence = FlashFuel.fromFourthStep(occurrence, fourthStep(request.anexo()).getFile());
            notifyNextStep(module, occurrence.getRegionalId(), occurrence.getCurrentStep(), occurrenceCode, moduleName);
        }

        occurrence.setCurrentStep(occurrence.getCurrentStep() + 1);
        occurrence.getStepLog().add(log);
        saveOccurrence(occurrence);
        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());
        return new GenericMessage("Avançado para próxima etapa", 200);
    }

    private AttachmentEntity fourthStep(MultipartFile anexo) {
        var attachment = createAttachment(List.of(new AttachmentRecord("pagamento", anexo)));
        return attachment.getFirst();
    }

    @Override
    public GenericMessage rejectOccurrence(String occurrenceId, Object dto, String userId) {
        FlashFuel occurrence = getOccurrenceInternal(occurrenceId);
        var request = (FlashFuelRequest) dto;
        checkStatus(occurrence);
        if (occurrence.getStatus() == DocumentStatus.APROVADO)
            throw new ModuleFailure("Não é possível rejeitar uma solicitação já finalizada.");

        occurrence.setStatus(DocumentStatus.REJEITADO);
        occurrence.setSituacao(DocumentStatus.FINALIZADO);
        occurrence.setCurrentStep(0);
        occurrence.setFinal_date(Date.from(Instant.now()));
        var log = createStepLog(UUID.fromString(userId),

                "Pedido Rejeitado", occurrence.getCurrentStep(), dto != null ? request.observacao() : "", null);
        addLog(occurrence, log);

        saveOccurrence(occurrence);
        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
        String occurrenceCode = String.valueOf(occurrence.getCodeID());
        String moduleName = module.getName();
        notifyApplicant(occurrence.getStepLog().stream().findFirst().get(), "Sua solicitação foi negada.",
                "<br><br>\n" + "\n" + "        O seu pedido foi negado.<br><br>\n" + "\n"
                        + "        Sua ocorrência foi negada! Acesse o sistema para mais detalhes.<br><br>\n",
                occurrenceCode, moduleName);

        return new GenericMessage("Ocorrência rejeitada com sucesso.", 200);
    }

    @Override
    public GenericMessage finalizeOccurrence(String occurrenceId, Object dto, String userId) {
        var request = (FlashFuelRequest) dto;
        FlashFuel occurrence = getOccurrenceInternal(occurrenceId);
        checkStatus(occurrence);
        FileMetadata metadata = null;
        if (request.anexos() != null) {
            List<AttachmentEntity> attachments = createAttachment(request.anexos());
            occurrence.getAttachments().addAll(attachments);
        }

        var changedOccurence = FlashFuel.fromFifthStep(occurrence, request);
        changedOccurence.setStatus(DocumentStatus.APROVADO);
        changedOccurence.setSituacao(DocumentStatus.FINALIZADO);
        changedOccurence.setCurrentStep(0);
        ;
        changedOccurence.setFinal_date(new Date());
        var log = createStepLog(UUID.fromString(userId), "Finalização", 0, request.observacao(), null);
        addLog(changedOccurence, log);

        saveOccurrence(changedOccurence);
        createOccurrenceLog(log, changedOccurence.getId(), changedOccurence.getCodeID());

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
        String occurrenceCode = String.valueOf(occurrence.getCodeID());
        String moduleName = module.getName();
        notifyApplicant(changedOccurence.getStepLog().stream().findFirst().get(), "Seu pedido foi finalizado.",
                "<br><br>\n" + "\n" + "        Seu pedido foi finalizado.<br><br>\n" + "\n"
                        + "        Sua ocorrência foi aprovada e finalizada.<br><br>\n",
                occurrenceCode, moduleName);

        return new GenericMessage("Ocorrência finalizada com sucesso.", 200);
    }

    @Override
    public FlashFuel getOccurrence(String id) {
        return getOccurrenceInternal(id);
    }

    @Override
    public Page<FlashFuel> listOccurrencesByUser(UUID userId, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.asc("status"), Sort.Order.asc("situacao")));

        return repository.findByApplicantId(userId, sortedPageable);
    }

    @Override
    public Page<FlashFuel> listOccurrencesAllowed(UUID userId, Pageable pageable) {
        Modulo modulo = moduleService.getModuleByID(UUID.fromString(modulo_id));
        ModuleResponseDTO module = moduleResponseMapper.toDTO(modulo, userId, false, false);

        Set<Integer> etapas = module.permissoesUsuario().etapasPermitidas();
        Set<Integer> regionais = module.permissoesUsuario().regionaisPermitidas();

        if (regionais.isEmpty()) {
            return Page.empty(pageable);
        }

        List<DocumentStatus> rejectStatus = List.of(DocumentStatus.REJEITADO, DocumentStatus.APROVADO);

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.asc("status"), Sort.Order.asc("situacao")));

        if (regionais.contains(0)) {
            return repository.findByCurrentStepInAndStatusNotIn(etapas, rejectStatus, sortedPageable);
        } else {
            return repository.findByCurrentStepInAndRegionalIdInAndStatusNotIn(etapas, regionais, rejectStatus,
                    sortedPageable);
        }
    }

    @Override
    public Page<FlashFuel> listAllAllowed(UUID userId, Pageable pageable) {
        Modulo modulo = moduleService.getModuleByID(UUID.fromString(modulo_id));
        ModuleResponseDTO module = moduleResponseMapper.toDTO(modulo, userId, false, false);

        Set<Integer> regionais = module.permissoesUsuario().regionaisPermitidas();

        // Se o usuário não tem permissão para nenhuma filial, retorna página vazia
        if (regionais.isEmpty()) {
            return Page.empty(pageable);
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.asc("status"), Sort.Order.asc("situacao")));

        // Se o usuário possui a permissão global (0), retorna todas as ocorrências sem
        // filtro de filial
        if (regionais.contains(0)) {
            return repository.findAll(sortedPageable);
        }

        // Filtra pelas filiais específicas às quais o usuário tem acesso
        return repository.findByRegionalIdIn(regionais, sortedPageable);
    }

    @Override
    public Page<FlashFuel> listAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<FlashFuel> listOccurrencesByFilial(Integer filial, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.asc("status"), Sort.Order.asc("situacao")));

        return repository.findByRegionalId(filial, sortedPageable);
    }

    @Override
    public void batchUpdateStatus(BatchStatusDTO ids, UUID user) {
        StepLog logger = createStepLog(user, "Rejeição em grupo", 0, ids.observacao() != null ? ids.observacao()
                : "Rejeição de " + ids.itens().size() + " itens sem observação.", null);
        List<IdCodeProjection> results = repository.findByIdIn(ids.itens());

        Map<String, Long> codeMap = results.stream()
                .collect(Collectors.toMap(IdCodeProjection::getId, IdCodeProjection::getCodeID));
        for (String id : ids.itens()) {
            Long codeID = codeMap.get(id);
            createOccurrenceLog(logger, id, codeID);
        }

        batchUpdateStatusGlobal(ids.itens(), logger, DocumentStatus.REJEITADO, FlashFuel.class);
    }

    private List<AttachmentEntity> createAttachment(List<AttachmentRecord> dtos) {
        List<AttachmentEntity> result = new ArrayList<>();
        for (AttachmentRecord dto : dtos) {
            var id = UUID.randomUUID().toString();

            var item = new AttachmentEntity();
            item.setNome(dto.nome());
            item.setId(id);
            String filename = item.getId() + " - "
                    + LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-");
            FileMetadata store = storeFile(dto.file(), "flash-combustivel/anexos", filename, 1);
            item.setFile(store);
            result.add(item);
        }
        return result;
    }

    private void createOccurrenceLog(StepLog log, String occurrenceID, Long code) {
        var occurrenceLog = new FlashFuelLog(occurrenceID, code);

        occurrenceLog.setId(UUID.randomUUID());
        occurrenceLog.setCreated_at(log.getCreated_at());
        occurrenceLog.setUser(log.getUser());
        occurrenceLog.setName(log.getName());
        occurrenceLog.setGroup(log.getGroup());
        occurrenceLog.setObservation(log.getObservation());
        occurrenceLog.setFinal_at(log.getFinal_at());

        logRepository.save(occurrenceLog);
    }
}
