package com.indux.modules.ppu.application.services.rdo.operation.audit.mio;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.organization_chart.application.services.ProjectService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.ppu.application.services.rdo.helper.LoggerUserHelper;
import com.indux.modules.ppu.domain.services.bm.audit.mio.AuditMio;
import com.indux.modules.ppu.domain.entities.bm.BMMioLog;
import com.indux.modules.ppu.domain.entities.bm.MioDivergenceJustification;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.entities.rdo.audit.MioDivergence;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.BoardedEmployeeMapper;
import com.indux.modules.ppu.infra.mio.EmployeeBoardingETL;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AuditMioImpl implements AuditMio {
    private final BoardedEmployeeMapper mapper;
    private final RDORepository repository;
    private final PPURepository ppuRepository;
    private final BMRepository bmRepository;
    private final EmployeeBoardingETL etl;
    private final ProjectService projectService;

    public AuditMioImpl(BoardedEmployeeMapper mapper, RDORepository repository, PPURepository ppuRepository,
            BMRepository bmRepository, EmployeeBoardingETL etl, ProjectService projectService) {
        this.mapper = mapper;
        this.repository = repository;
        this.ppuRepository = ppuRepository;
        this.bmRepository = bmRepository;
        this.etl = etl;
        this.projectService = projectService;
    }

    @Override
    public List<MioDivergence> call(String bmID, JwtAuthenticationToken token)
            throws IOException, ExecutionException, InterruptedException {
        var bm = bmRepository.findById(bmID)
                .orElseThrow(() -> new ModuleNotFoundFailure("BM não encontrada para essa auditoria."));
        var ppu = ppuRepository.findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO)
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada para este projeto"));
        var project = projectService.getbyId(ppu.getProjectId());
        var ids = project.getFilial().stream().map(detail -> detail.getFilialId()).toList();
        var platforms = ppu.getPlatforms();
        var rdos = repository.findAllByPlatformInAndStatusOPAndDateBetween(platforms, RDOStatusOP.APPROVED,
                bm.getPeriod().getStart(), bm.getPeriod().getEnd().minusDays(1));
        var loggerUser = LoggerUserHelper.createUser(token);
        String start = bm.getPeriod().getStart().toString();
        String end = bm.getPeriod().getEnd().toString();

        List<BoardedEmployee> mioBoarded = etl.fetchBoardedEmployees(null, null, start, end);

        CompletableFuture<Map<LocalDate, List<BoardedEmployee>>> groupingFromMIO = CompletableFuture
                .supplyAsync(() -> groupEmployeeFromMIOByDate(mioBoarded, ppu.getPlatforms(), ids, bm.getPeriod()));

        CompletableFuture<Map<LocalDate, List<BoardedEmployee>>> groupingFromRDO = CompletableFuture
                .supplyAsync(() -> groupEmployeesFromRDOByDate(rdos));

        CompletableFuture.allOf(groupingFromMIO, groupingFromRDO).join();

        var groupRDO = groupingFromRDO.get();
        var groupMIO = groupingFromMIO.get();

        var divergences = analyzePresence(groupRDO, groupMIO);

        if (!bm.getMioJustifications().isEmpty()) {
            Map<String, MioDivergenceJustification> justifiedMap = bm.getMioJustifications().stream()
                    .collect(Collectors.toMap(
                            j -> j.getRegistration() + "|" + j.getDate(),
                            j -> j,
                            (a, b) -> a));

            divergences.forEach(d -> {
                String key = d.getRegistration() + "|" + d.getDate();
                if (justifiedMap.containsKey(key)) {
                    d.setJustified(true);
                    d.setJustification(justifiedMap.get(key).getJustification());
                }
            });

            Set<String> currentDivergenceKeys = divergences.stream()
                    .map(d -> d.getRegistration() + "|" + d.getDate())
                    .collect(Collectors.toSet());

            justifiedMap.forEach((key, j) -> {
                if (!currentDivergenceKeys.contains(key)) {
                    divergences.add(MioDivergence.builder()
                            .date(j.getDate())
                            .registration(j.getRegistration())
                            .statusMIO("PRESENTE")
                            .statusRDO("PRESENTE")
                            .justified(true)
                            .justification(j.getJustification())
                            .build());
                }
            });
        }

        if (!bm.isFinished()) {
            var log = createBmLog(loggerUser, divergences);
            bm.getAuditMIOLog().add(log);
            bm.setUpdatedAt(Instant.now());
            if (divergences.isEmpty())
                bm.setAuditMIOChecked(true);
            bmRepository.save(bm);
        }

        return divergences;
    }

    private BMMioLog createBmLog(RDOLoggerUser user, List<MioDivergence> result) {
        return BMMioLog.builder()
                .auditAt(Instant.now())
                .success(result.isEmpty())
                .divergences(result)
                .user(user)
                .build();
    }

    Map<LocalDate, List<BoardedEmployee>> groupEmployeesFromRDOByDate(List<RDOEntity> rdos) {
        if (rdos == null || rdos.isEmpty())
            return Collections.emptyMap();

        return rdos.stream()
                .collect(Collectors.groupingBy(RDOEntity::getDate, HashMap::new, Collectors.flatMapping(rdo -> {
                    List<RDOServiceEntity> services = Optional.ofNullable(rdo.getServices())
                            .orElseGet(Collections::emptyList).stream()
                            .filter(service -> service.getDisposicao() == null || !service.getDisposicao()).toList();

                    String platform = rdo.getPlatform();
                    String rdoId = rdo.getId();

                    var serviceEmployees = mapper.fromRDOServiceList(services, platform, rdoId).stream();

                    var departureEmployees = mapper.fromRDOEmployeeDepartureList(
                            Optional.ofNullable(rdo.getEmployeeDepartures()).orElseGet(Collections::emptyList),
                            platform, rdoId).stream();

                    var allEmps = Stream.concat(serviceEmployees, departureEmployees).toList();
                    return allEmps.stream();
                }, Collectors.toCollection(ArrayList::new))));
    }

    private Map<LocalDate, List<BoardedEmployee>> groupEmployeeFromMIOByDate(List<BoardedEmployee> request,
            List<String> plataformas, List<Integer> branchsID, DateRange range) {
        if (request == null || request.isEmpty())
            return Collections.emptyMap();

        Map<LocalDate, Map<String, List<BoardedEmployee>>> tempGroup = new HashMap<>();
        LocalDate rangeStart = range.getStart();
        LocalDate rangeEnd = range.getEnd();

        for (BoardedEmployee emp : request) {
            if (notValid(plataformas, branchsID, emp)) {
                continue;
            }

            LocalDate start = emp.getBoarding();
            if (start == null) {
                continue;
            }

            LocalDate end = emp.getLandingForecast();
            if (end == null)
                end = start;
            if (end.isBefore(start)) {
                LocalDate tmp = start;
                start = end;
                end = tmp;
            }

            LocalDate effectiveStart = start.isBefore(rangeStart) ? rangeStart : start;
            LocalDate effectiveEnd = end.isAfter(rangeEnd) ? rangeEnd : end;

            if (!effectiveStart.isAfter(effectiveEnd)) {
                for (LocalDate d = effectiveStart; !d.isAfter(effectiveEnd); d = d.plusDays(1)) {
                    tempGroup.computeIfAbsent(d, __ -> new HashMap<>())
                            .computeIfAbsent(emp.getRegistration(), __ -> new ArrayList<>()).add(emp);
                }
            }
        }

        Map<LocalDate, List<BoardedEmployee>> group = new HashMap<>();
        for (Map.Entry<LocalDate, Map<String, List<BoardedEmployee>>> dateEntry : tempGroup.entrySet()) {
            LocalDate date = dateEntry.getKey();
            Map<String, List<BoardedEmployee>> employeesByRegistration = dateEntry.getValue();

            List<BoardedEmployee> dayEmployees = new ArrayList<>();
            for (List<BoardedEmployee> employees : employeesByRegistration.values()) {
                BoardedEmployee any = employees.get(0);
                dayEmployees.add(any);
            }

            group.put(date, dayEmployees);
        }

        return group;
    }

    private boolean notValid(List<String> plataformas, List<Integer> branchsID, BoardedEmployee emp) {
        var cc = extractLeadingInt(emp.getFilial_HCM()).orElse(null);
        if (cc == null || !branchsID.contains(cc)) {
            return true;
        }

        String plat = normalizePlatform(emp.getPlatform());

        if (isTimesheetEventCode(plat)) {
            return false;
        }

        List<String> normalizedPlatforms = plataformas.stream().filter(Objects::nonNull)
                .map(AuditMioImpl::normalizePlatform).toList();

        if (plat != null && !plat.isBlank() && !normalizedPlatforms.contains(plat)) {
            return true;
        }

        return false;
    }

    private boolean isTimesheetEventCode(String platform) {
        if (platform == null)
            return false;
        return platform.equals("ASO") || platform.equals("F") || platform.equals("DBA") || platform.equals("DOBRA")
                || platform.equals("HTV") || platform.equals("TF") || platform.equals("FALTA");
    }

    List<MioDivergence> analyzePresence(Map<LocalDate, List<BoardedEmployee>> rdoData,
            Map<LocalDate, List<BoardedEmployee>> mioData) {
        List<MioDivergence> divergences = new ArrayList<>();

        Set<LocalDate> allDates = new HashSet<>();
        allDates.addAll(rdoData.keySet());
        allDates.addAll(mioData.keySet());

        for (LocalDate date : allDates) {
            List<BoardedEmployee> rdoEmployees = rdoData.getOrDefault(date, List.of());
            List<BoardedEmployee> mioEmployees = mioData.getOrDefault(date, List.of());

            Map<String, BoardedEmployee> rdoByRegistration = rdoEmployees.stream()
                    .collect(Collectors.toMap(BoardedEmployee::getRegistration, e -> e, (e1, e2) -> e1));

            Map<String, BoardedEmployee> mioByRegistration = mioEmployees.stream()
                    .collect(Collectors.toMap(BoardedEmployee::getRegistration, e -> e, (e1, e2) -> e1));

            Map<String, String> rdoIdByPlatform = rdoEmployees.stream()
                    .collect(Collectors.toMap(
                            BoardedEmployee::getPlatform,
                            BoardedEmployee::getRdoId,
                            (id1, id2) -> id1));

            Set<String> allRegistrations = new HashSet<>();
            allRegistrations.addAll(rdoByRegistration.keySet());
            allRegistrations.addAll(mioByRegistration.keySet());

            for (String registration : allRegistrations) {
                BoardedEmployee rdoEmp = rdoByRegistration.get(registration);
                BoardedEmployee mioEmp = mioByRegistration.get(registration);

                if (rdoEmp != null && mioEmp != null) {
                    continue;
                }

                String statusRDO;
                String statusMIO;
                String platform;
                String name;
                String rdoId;

                if (rdoEmp != null) {
                    statusRDO = "PRESENTE";
                    statusMIO = "AUSENTE";
                    platform = rdoEmp.getPlatform();
                    name = rdoEmp.getName();
                    rdoId = rdoEmp.getRdoId();
                } else {
                    boolean landingDay = isLandingDay(mioEmp, date);
                    statusRDO = landingDay ? "DESEMBARQUE AUSENTE" : "AUSENTE";
                    statusMIO = "PRESENTE";
                    platform = mioEmp.getPlatform();
                    name = mioEmp.getName();
                    rdoId = rdoIdByPlatform.get(platform);
                }

                divergences.add(MioDivergence.builder()
                        .date(date)
                        .idRDO(rdoId)
                        .platform(platform)
                        .registration(registration)
                        .name(name)
                        .statusRDO(statusRDO)
                        .statusMIO(statusMIO)
                        .build());
            }
        }

        return divergences;
    }

    private boolean isLandingDay(BoardedEmployee emp, LocalDate date) {
        if (emp == null)
            return false;
        LocalDate landing = emp.getLandingForecast();
        return landing != null && date.equals(landing);
    }

    public Optional<Integer> extractLeadingInt(String filialHcm) {
        if (filialHcm == null)
            return Optional.empty();
        Matcher m = Pattern.compile("^\\s*(\\d+)").matcher(filialHcm);
        return m.find() ? Optional.of(Integer.valueOf(m.group(1))) : Optional.empty();
    }

    private static String normalizePlatform(String platform) {
        if (platform == null)
            return null;
        return platform.trim().toUpperCase().replaceAll("[\\s\\-_]+", "").replaceAll("[^A-Z0-9]", "");
    }

}
