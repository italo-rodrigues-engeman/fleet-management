package com.indux.modules.ppu.application.services.rdo;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.service.module.ModulePermissionChecker;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.utils.Competence;
import com.indux.modules.clients.domain.model.Client;
import com.indux.modules.clients.domain.repository.ClientRepository;
import com.indux.modules.organization_chart.application.services.ProjectService;
import com.indux.modules.ppu.application.dtos.NextRdoDTO;
import com.indux.modules.ppu.domain.entities.rdo.AccessoryKitDTO;
import com.indux.modules.ppu.domain.entities.rdo.SteelCableChecker;
import com.indux.modules.ppu.application.dtos.requests.CreateRDOJustification;
import com.indux.modules.ppu.application.dtos.requests.RDOFilter;
import com.indux.modules.ppu.application.dtos.requests.RDOFlowRequest;
import com.indux.modules.ppu.application.projection.RDOGridProjection;
import com.indux.modules.ppu.application.dtos.rdo.RDORecord;
import com.indux.modules.ppu.application.dtos.rdo.RDOResponse;
import com.indux.modules.ppu.application.dtos.response.RDOPageResponse;
import com.indux.modules.ppu.application.services.rdo.helper.RDOTotalPlannedHelper;
import com.indux.modules.ppu.application.services.rdo.helper.RDOTimeChangeAnalyzer;
import com.indux.modules.ppu.application.services.rdo.helper.WorkHoursHelper;
import com.indux.modules.ppu.application.services.rdo.operation.FetchRDOUseCase;
import com.indux.modules.ppu.application.services.rdo.operation.RDOApprovalUseCase;
import com.indux.modules.ppu.application.services.rdo.operation.RDOUpdaterUseCase;
import com.indux.modules.ppu.application.services.rdo.rh.PayrollOccurrenceService;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.LinePPU;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.*;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLogger;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerType;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.domain.services.UpdateCheckDiff;
import com.indux.modules.ppu.domain.strategy.time.ProjectionContext;
import com.indux.modules.ppu.domain.strategy.time.TimeStrategyRunner;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import com.indux.modules.ppu.infra.mapper.RDOLoggerUserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RDOService extends RDOTotalPlannedHelper {
    private final RDORepository repository;
    private final UserService userService;
    private final PPURepository ppuRepository;
    private final ClientRepository clientRepository;
    private final RDOLoggerUserMapper mapper;
    private final RDOUpdaterUseCase updater;
    private final RDOApprovalUseCase approval;
    private final FetchRDOUseCase fetchUseCase;
    private final WorkHoursHelper workHoursEngine;
    private final ProjectService projectService;
    private final TimeStrategyRunner timeStrategyRunner;
    private final LineStrategiesApplier lineApplier;
    private final PayrollOccurrenceService payrollOccurrenceService;
    private final RDOTimeChangeAnalyzer timeChangeAnalyzer;
    private final ModulePermissionChecker permissionChecker;
    @Value("${module.rdo.id}")
    private String opRdoModuleId;

    public RDOService(RDORepository repository, UserService userService, PPURepository ppuRepository,
            ClientRepository clientRepository, RDOUpdaterUseCase updater, RDOApprovalUseCase approval,
            FetchRDOUseCase fetchUseCase, RDOLoggerUserMapper mapper, WorkHoursHelper workHoursEngine,
            ProjectService projectService, LineStrategiesApplier lineApplier,
            TimeStrategyRunner timeStrategyRunner, PayrollOccurrenceService payrollOccurrenceService,
            RDOTimeChangeAnalyzer timeChangeAnalyzer, ModulePermissionChecker permissionChecker) {
        this.repository = repository;
        this.userService = userService;
        this.ppuRepository = ppuRepository;
        this.clientRepository = clientRepository;
        this.mapper = mapper;
        this.updater = updater;
        this.approval = approval;
        this.fetchUseCase = fetchUseCase;
        this.workHoursEngine = workHoursEngine;
        this.projectService = projectService;
        this.timeStrategyRunner = timeStrategyRunner;
        this.lineApplier = lineApplier;
        this.payrollOccurrenceService = payrollOccurrenceService;
        this.timeChangeAnalyzer = timeChangeAnalyzer;
        this.permissionChecker = permissionChecker;
    }


    public CreateRDOResult create(RDORecord dto, UUID currentUser) {
        var ppu = ppuRepository.findById(dto.PPUid())
                .orElseThrow(() -> new ModuleNotFoundFailure("Não foi encontrada nenhuma PPU para este RDO."));

        if (repository.existsByPlatformAndPpuIdAndDate(dto.plataforma(), ppu.getId(), dto.data()))
            throw new ModuleFailure("Já existe RDO para o dia " + dto.data());
        Long sequenceID = calculateNextSequenceId(dto.plataforma(), ppu.getId(), dto.data());
        var currentEmployee = userService.getEmployeeFromUser(currentUser);
        var loggerUser = mapper.toLogger(currentEmployee, currentUser);

        RDORecord enrichedDto = attachPPUIds(dto, ppu);
        Client client = clientRepository.findById(ppu.getClientId()).orElse(null);

        var entity = RDOEntity.fromDTOCreate(
                enrichedDto, sequenceID, currentEmployee,
                ppu,
                client != null ? client.getName() : "Cliente não informado");

        validateHours(entity);
        applyTimeStrategies(entity, ppu);
        lineApplier.apply(entity,ppu);
        addTotalPlanned(entity, ppu);

        entity.setLoggers(List.of(createLogger(loggerUser, dto.justificativa(), dto.justificativaCoordenador())));
        entity.setCreatedAt(LocalDateTime.now());

        RDOEntity saved = repository.save(entity);
        return new CreateRDOResult(sequenceID, saved.getId());
    }

    public record CreateRDOResult(Long sequentialCode, String id) {}

    private void applyTimeStrategies(RDOEntity entity, PPUEntity ppu) {
        var base = Optional.ofNullable(entity.getServices()).orElse(List.of());
        if (base.isEmpty())
            return;

        var ppuServices = Optional.ofNullable(ppu.getServices()).orElse(List.of());
        var result = timeStrategyRunner.run(TimeStrategyPhase.CREATE, ProjectionContext.SAVE, base, ppuServices);

        entity.setServices(result.working());
    }

    private static void addTotalPlanned(RDOEntity entity, PPUEntity ppu) {
        entity.setTotalPlannedEquipments(enrichEquipmentWithPlanned(entity.getPlatform(), ppu.getEquipments()));
        entity.setSteelCable(
                enrichSteelCableWithPlanned(entity.getPlatform(), entity.getSteelCable(), ppu.getSteelCables()));
        entity.setAccessoryKits(enrichAccessoryKitsWithPlanned(entity.getPlatform(), entity.getAccessoryKits(),
                ppu.getAccessoryKits()));
    }

    private RDORecord attachPPUIds(RDORecord dto, PPUEntity ppu) {
        String platform = dto.plataforma();
        Map<String, String> steelByNameAndPlatform = ppu.getSteelCables().stream()
                .collect(Collectors.toMap(
                        line -> (line.getPlatforms() + "|" + line.getName()).toLowerCase(),
                        LinePPU::getId,
                        (a, b) -> a));
        Map<String, String> equipByName = ppu.getEquipments().stream()
                .collect(Collectors.toMap(line -> line.getName().toLowerCase(), LinePPU::getId, (a, b) -> a));

        var steelCables = Optional.ofNullable(dto.cabosDeAcos()).orElse(List.of())
                .stream()
                .map(sc -> {
                    String key = (platform + "|" + sc.nome()).toLowerCase();
                    String id = steelByNameAndPlatform.get(key);
                    return id != null ? sc.copyWithLineID(id) : sc;
                })
                .toList();

        var equipments = Optional.ofNullable(dto.equipamentos()).orElse(List.of())
                .stream()
                .map(eq -> {
                    String id = equipByName.get(eq.nome().toLowerCase());
                    if (id != null)
                        eq.copyWithID(id);
                    return eq;
                })
                .toList();

        return dto.copyWithUpdatedItems(steelCables, equipments);
    }

    private void validateHours(RDOEntity entity) {
        if (entity == null || entity.getServices() == null)
            return;

        for (var service : entity.getServices()) {
            if (service == null)
                continue;
            var shift = service.getSchedule();
            if (shift == null || shift.getHorarioInicial() == null || shift.getHorarioFinal() == null) {
                resetHours(service);
                continue;
            }
            if (Boolean.TRUE.equals(service.getDisposicao())) {
                resetHours(service);
            } else {
                workHoursEngine.applyTo(service);
            }
        }
    }

    private static void resetHours(RDOServiceEntity service) {
        service.setOvertimes(Collections.emptyList());
        service.setNormalHours(Duration.ofHours(0));
        service.setHourTotais(Duration.ofHours(0));
        service.setOvertimeHourTotais(Duration.ofHours(0));
        service.setNightShiftPremium(Duration.ofHours(0));
    }

    private boolean hasOvertime(RDOServiceEntity service) {
        return service != null
                && service.getOvertimeHourTotais() != null
                && service.getOvertimeHourTotais().toMinutes() > 0;
    }

    private RDOLogger createLogger(RDOLoggerUser user, String justification,
            CreateRDOJustification coordinatorJustification) {
        final String sector = "OP";
        return RDOLogger.builder()
                .wasAnalyzed(true)
                .isInfoCorrect(true)
                .action(RDOLoggerType.CREATION)
                .justification(justification)
                .coordinatorJustification(coordinatorJustification)
                .date(LocalDateTime.now())
                .sector(sector)
                .user(user)
                .build();
    }

    public RDOEntity updateComplete(String id, RDORecord payload, String userId, String authToken) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("RDO não encontrada no sistema."));
        SimpleUser user = userService.getUserById(userId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Usuário não encontrado."));

        UpdateCheckDiff.Snapshot beforeSnapshot = null;
        if (RDOStatusDP.CLOSED_COMPETENCE.equals(entity.getStatusDP())) {
            beforeSnapshot = UpdateCheckDiff.snapshotByRegistration(entity.getServices());
        }

        var edited = updater.applyUpdate(payload, entity, user);

        var ppu = ppuRepository.findById(edited.getPpuId())
                .orElseThrow(() -> new ModuleNotFoundFailure("Não foi encontrada nenhuma PPU para este RDO."));

        validateHours(edited);
        purgeGeneratedServices(edited);
        applyTimeStrategies(edited, ppu);
        lineApplier.apply(edited, ppu);
        addTotalPlanned(edited, ppu);

        if (RDOStatusDP.CLOSED_COMPETENCE.equals(edited.getStatusDP()) && beforeSnapshot != null) {
            UpdateCheckDiff.Snapshot afterSnapshot = UpdateCheckDiff.snapshotByRegistration(edited.getServices());
            var changes = UpdateCheckDiff.diff(beforeSnapshot, afterSnapshot);
            if (!changes.isEmpty()) {
                handleClosedCompetenceTimeChanges(edited, changes, authToken);
            }
        }

        return repository.save(edited);
    }

    private void purgeGeneratedServices(RDOEntity entity) {
        if (entity.getServices() == null)
            return;
        entity.setServices(
                entity.getServices().stream()
                        .filter(s -> !Boolean.TRUE.equals(s.getGenerated()))
                        .collect(Collectors.toList()));
    }

    private void handleClosedCompetenceTimeChanges(RDOEntity rdo, List<UpdateCheckDiff.Change> changes,
            String authToken) {
        if (rdo.getCompetence() == null || rdo.getCompetence().isEmpty()) {
            return;
        }

        String competenceFormatted = payrollOccurrenceService.formatCompetenceToMMyyyy(rdo.getCompetence());

        var separated = timeChangeAnalyzer.separateChanges(changes);

        for (UpdateCheckDiff.Change change : separated.increases()) {
            payrollOccurrenceService.createOccurrenceForChange(
                    rdo,
                    change,
                    "Pagamento a Maior",
                    competenceFormatted,
                    authToken);
        }

        for (UpdateCheckDiff.Change change : separated.decreases()) {
            payrollOccurrenceService.createOccurrenceForChange(
                    rdo,
                    change,
                    "Pagamento a Menor",
                    competenceFormatted,
                    authToken);
        }
    }

    public NextRdoDTO operationApproval(RDOFlowRequest request, Boolean nextItem, String userID) {
        var rdoApproved = approval.approve(request, nextItem, userID);

        if (nextItem) {
            Optional<RDOEntity> nextOpt = repository
                    .findFirstByPlatformAndStatusDPOrderBySequentialIdAsc(
                            rdoApproved.getPlatform(),
                            RDOStatusDP.PENDING);
            var nextId = nextOpt.map(RDOEntity::getId).orElse(null);
            if (nextId != null && nextId.equals(request.rdo())) {
                return null;
            }

            return new NextRdoDTO(nextId);
        }
        return null;
    }

    public Long calculateNextSequenceId(String platform, String ppuId, LocalDate date) {
        var previousEntity = repository.findTopByPlatformAndPpuIdAndDateBeforeOrderByDateDesc(platform, ppuId, date)
                .orElse(null);
        if (previousEntity == null)
            return 1L;

        int quantityMissing = getDatesWithoutRDOByPlatform(platform, ppuId, previousEntity.getDate(), date).size();

        return previousEntity.getSequentialId() + quantityMissing;
    }

    public List<LocalDate> getDatesWithoutRDOByPlatform(String platform, String ppuId, LocalDate startDate, LocalDate endDate) {
        List<RDOEntity> rdos = repository.findAllByPlatformAndPpuIdAndDateBetween(platform, ppuId, startDate, endDate);

        Set<LocalDate> existingDates = rdos.stream()
                .map(RDOEntity::getDate)
                .collect(Collectors.toSet());

        List<LocalDate> allDates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            allDates.add(date);
        }

        return allDates.stream()
                .filter(d -> !existingDates.contains(d))
                .toList();
    }

    public Page<RDOResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(fetchUseCase::toSimpleResponse);
    }

    public RDOPageResponse findAllOp(UUID userId, Pageable pageable) {
        var perms = permissionChecker.getAllowedRegionaisAndProjects(
                UUID.fromString(opRdoModuleId), userId);

        Set<Integer> regionais = perms.regionais();
        Set<Integer> projetos = perms.projetos();

        boolean filterProjects = !projetos.isEmpty() && !projetos.contains(0);
        List<Long> projectsForCount = filterProjects
                ? projetos.stream().map(Integer::longValue).toList()
                : List.of();

        var filter = RDOFilter.ofProject(projectsForCount);
        var approvePending = repository.countByFiltersOP(RDOStatusOP.PENDING, null, null, null, filter);
        var correctionPending = repository.countByFiltersOP(RDOStatusOP.CORRECTION, null, null, null, filter);

        var page = repository.findAllOrdered(pageable, regionais, projetos);
        return new RDOPageResponse(
                page,
                approvePending,
                correctionPending);
    }

    public Page<RDOResponse> fetchAllByPPU(String ppuID, Pageable pageable) {
        PPUEntity ppu = ppuRepository.findById(ppuID)
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada: " + ppuID));
        return repository.findAllByPpuId(ppuID, pageable)
                .map(entity -> fetchUseCase.toResponse(entity, ppu));
    }

    public List<RDOEntity> fetchAllByPpuAndDate(String ppuId, LocalDate date) {
        ppuRepository.findById(ppuId)
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada: " + ppuId));

        return repository.findAllByPpuIdAndDateOrderByPlatformAsc(ppuId, date);
    }

    public List<RDOEntity> approveBatchByPpuAndDate(String ppuId, LocalDate date, String userId) {
        List<RDOEntity> rdos = fetchAllByPpuAndDate(ppuId, date);
        if (rdos.isEmpty()) {
            throw new ModuleNotFoundFailure("Nenhum RDO encontrado para a PPU e data informadas.");
        }

        SimpleUser user = userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado."));
        RDOLoggerUser loggerUser = mapper.toLogger(user);

        for (RDOEntity rdo : rdos) {
            rdo.setStatusOP(RDOStatusOP.APPROVED);
            rdo.setStatusDP(RDOStatusDP.PENDING);

            if (rdo.getLoggers() == null) {
                rdo.setLoggers(new ArrayList<>());
            }
            rdo.getLoggers().add(createBatchExpressApprovalLog(loggerUser));
        }

        return repository.saveAll(rdos);
    }

    private RDOLogger createBatchExpressApprovalLog(RDOLoggerUser user) {
        final String sector = "OP";
        return RDOLogger.builder()
                .wasAnalyzed(true)
                .isInfoCorrect(true)
                .action(RDOLoggerType.APPROVAL)
                .justification("Aprovado via RDO Lote Express")
                .date(LocalDateTime.now())
                .sector(sector)
                .user(user)
                .build();
    }

    public RDOResponse fetchByCodeAndPlatform(Long code, String platform) {
        RDOEntity entity = repository.findBySequentialIdAndPlatform(code, platform);
        if (entity == null) {
            throw new ModuleNotFoundFailure("RDO não encontrado para o código " + code + " e plataforma " + platform);
        }
        return fetchUseCase.toResponse(entity);
    }

    public RDOResponse fetchByID(String id) {
        return fetchUseCase.execute(id);
    }

    public Page<RDOGridProjection> listByCompetence(String platform, YearMonth competencePeriod, Pageable pageable) {
        LocalDate start = competencePeriod.atDay(16);
        LocalDate end = competencePeriod.plusMonths(1).atDay(15);

        return repository.findAllByPlatformAndStatusOPAndDateBetween(
                platform, RDOStatusOP.APPROVED, start, end, pageable);
    }

    public Page<RDOGridProjection> listByDateWithinCompetence(String platform, LocalDate anyDate, Pageable pageable) {
        Competence c = Competence.ofDate(anyDate);
        return listByCompetence(platform, c.getPeriod(), pageable);
    }

    public void deleteRDO(String rdoID) {
        repository.deleteById(rdoID);
    }

    public RDOPageResponse filterRDO(
            RDOFilter filter, Pageable pageable, Boolean isRH) {
        CounterResult counters = null;
        Page<RDOGridProjection> result = null;
        if (filter.organograma() != null && !filter.organograma().isEmpty()) {
            List<Long> projetcsIds = projectService.getProjectIdsByOrganization(filter.organograma());
            List<Long> ids = new ArrayList<>(projetcsIds);
            if (filter.projeto() != null && !filter.projeto().isEmpty()) {
                for (Long projetcId : filter.projeto()) {
                    ids.add(projetcId);
                }
            }
            if (projetcsIds.isEmpty()) {
                ids.clear();
                ids.add(0L);
            }
            var newFilter = filter.withProject(ids);
            counters = counterPending(newFilter, isRH);
            result = repository.filter(newFilter, pageable, isRH);
        }
        if (counters == null || result == null) {
            counters = counterPending(filter, isRH);
            result = repository.filter(filter, pageable, isRH);
        }
        return new RDOPageResponse(
                result,
                counters.approvePending(),
                counters.correctionPending());
    }

    private CounterResult counterPending(RDOFilter filter, boolean isRH) {
        var nf = NormalizedFilter.of(filter);
        Integer approve;
        Integer correction;
        if (isRH) {
            approve = repository.countByFiltersRH(
                    RDOStatusDP.PENDING,
                    List.of(RDOStatusOP.PENDING, RDOStatusOP.CORRECTION),
                    nf.platform(), nf.start(), nf.end(), filter);
            correction = repository.countByFiltersRH(
                    RDOStatusDP.CORRECTION_OP,
                    List.of(),
                    nf.platform(), nf.start(), nf.end(), filter);
        } else {
            approve = repository.countByFiltersOP(
                    RDOStatusOP.PENDING,
                    nf.platform(), nf.start(), nf.end(), filter);
            correction = repository.countByFiltersOP(
                    RDOStatusOP.CORRECTION,
                    nf.platform(), nf.start(), nf.end(), filter);
        }
        return new CounterResult(approve, correction);
    }

    // script to update all rdos if necessary
    // sets default values for missing fields and recalculates hours
    // returns a report with total and changed records
    public UpdateReport updateHoursAndReport() {
        var all = repository.findAll();
        long changed = 0;
        List<RDOEntity> toSave = new ArrayList<>(all.size());

        for (var rdo : all) {
            if (rdo == null || rdo.getServices() == null)
                continue;

            boolean mutated = false;
            for (var s : rdo.getServices()) {
                if (s == null)
                    continue;
                if (s.getDisposicao() == null) {
                    s.setDisposicao(Boolean.FALSE);
                    mutated = true;
                }
                if (s.getOvertimes() == null) {
                    s.setOvertimes(Collections.emptyList());
                    mutated = true;
                }
            }

            var before = fp(rdo);
            validateHours(rdo);
            var after = fp(rdo);

            if (mutated || !before.equals(after)) {
                toSave.add(rdo);
                changed++;
            }
        }

        if (!toSave.isEmpty())
            repository.saveAll(toSave);
        return new UpdateReport(all.size(), changed);
    }

    public record UpdateReport(long total, long changed) {
    }

    private String fp(RDOEntity rdo) {
        long night = 0, total = 0, ot = 0;
        if (rdo.getServices() != null) {
            for (var s : rdo.getServices()) {
                if (s == null)
                    continue;
                night += mins(s.getNightShiftPremium());
                total += mins(s.getHourTotais());
                ot += mins(s.getOvertimeHourTotais());
            }
        }
        return night + ":" + total + ":" + ot;
    }

    private long mins(Duration d) {
        return d == null ? 0 : d.toMinutes();
    }

    private record CounterResult(Integer approvePending, Integer correctionPending) {
    }

    private record NormalizedFilter(String platform, LocalDate start, LocalDate end) {
        static NormalizedFilter of(RDOFilter f) {
            String platform = (f.plataforma() != null && !f.plataforma().isBlank()) ? f.plataforma() : null;
            LocalDate start = f.dataInicio();
            LocalDate end = f.dataFim();
            if (start != null && end == null)
                end = start;
            if (end != null && start == null)
                start = end;
            if (start != null && end.isBefore(start)) {
                var tmp = start;
                start = end;
                end = tmp;
            }
            return new NormalizedFilter(platform, start, end);
        }
    }

    public void update() {
        var allRdos = repository.findAll();
        var allPpus = ppuRepository.findAll();

        // Mapa PPUId -> PPUEntity
        Map<String, PPUEntity> ppuById = allPpus.stream()
                .collect(Collectors.toMap(PPUEntity::getId, Function.identity()));

        for (var rdo : allRdos) {
            var myPPU = ppuById.get(rdo.getPpuId());
            if (myPPU == null)
                continue;

            String platform = rdo.getPlatform();
            if (platform == null)
                continue;

            // --- Atualizar Equipamentos ---
            Optional.ofNullable(rdo.getEquipments()).orElse(List.of()).forEach(equip -> {
                var match = myPPU.getEquipments().stream()
                        .filter(ppuEq -> ppuEq.getName().equalsIgnoreCase(equip.getName()))
                        .findFirst();

                match.ifPresent(ppuEq -> equip.setEquipmentPPUId(ppuEq.getId()));
            });

            // --- Atualizar Cabos de Aço ---
            List<SteelCableChecker> updatedCables = Optional.ofNullable(rdo.getSteelCable())
                    .orElse(List.of())
                    .stream()
                    .map(cable -> {
                        var match = myPPU.getSteelCables().stream()
                                .filter(ppuCable -> ppuCable.getPlatforms() != null
                                        && ppuCable.getPlatforms().contains(platform))
                                .filter(ppuCable -> ppuCable.getName().equalsIgnoreCase(cable.nome()))
                                .findFirst();
                        return match.map(steelCableLine -> cable.copyWithLineID(steelCableLine.getId())).orElse(cable);
                    })
                    .toList();
            rdo.setSteelCable(updatedCables);

            // --- Atualizar Kits de Acessórios ---
            List<AccessoryKitDTO> updatedKits = Optional.ofNullable(rdo.getAccessoryKits())
                    .orElse(List.of())
                    .stream()
                    .map(kit -> {
                        var match = myPPU.getAccessoryKits().stream()
                                .filter(ppuKit -> ppuKit.getName().equalsIgnoreCase(kit.nome()))
                                .findFirst();
                        if (match.isPresent()) {
                            return kit.copyWithID(match.get().getId());
                        }
                        return kit;
                    })
                    .toList();
            rdo.setAccessoryKits(updatedKits);

            repository.save(rdo);
        }
    }

}
