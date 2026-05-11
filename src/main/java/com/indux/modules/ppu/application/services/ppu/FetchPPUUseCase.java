package com.indux.modules.ppu.application.services.ppu;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.model.employee.EmployeePosition;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.service.generic.GetPositionService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.modules.organization_chart.application.services.ProjectService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.application.dtos.requests.CreateRDOCoordinatorRequest;
import com.indux.modules.ppu.application.services.rdo.RDOService;
import com.indux.modules.ppu.application.services.rdo.operation.audit.mio.AuditMioImpl;
import com.indux.modules.ppu.domain.entities.item.ServicePosition;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.LinePPU;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.domain.services.PlatformFilter;
import com.indux.modules.ppu.infra.mapper.PPUResponseMapper;
import com.indux.modules.ppu.infra.mio.EmployeeBoardingETL;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FetchPPUUseCase {
    private static final String DEFAULT_STATUS = "Embarque";

    private final EmployeeBoardingETL etl;
    private final UserService userService;
    private final PPURepository repository;
    private final RDORepository rdo;
    private final RDOService rdoService;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final PPUResponseMapper ppuResponseMapper;
    private final GetPositionService<EmployeePosition> getPositionService;
    private final ProjectService projectService;

    public FetchPPUUseCase(EmployeeBoardingETL etl, UserService userService, PPURepository repository, RDORepository rdo, RDOService rdoService, GetEmployeeUseCase getEmployeeUseCase, PPUResponseMapper ppuResponseMapper, GetPositionService<EmployeePosition> getPositionService, ProjectService projectService) {
        this.etl = etl;
        this.userService = userService;
        this.repository = repository;
        this.rdo = rdo;
        this.rdoService = rdoService;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.ppuResponseMapper = ppuResponseMapper;
        this.getPositionService = getPositionService;
        this.projectService = projectService;
    }

    public PPUResponse fetchForSupervisor(UUID userID, LocalDate when)
            throws ExecutionException, InterruptedException {

        LocalDate targetDate = resolveTargetDate(when);
        LocalDate today = LocalDate.now();
        if (!targetDate.equals(today.minusDays(1))) {
            throw new ModuleFailure("Supervisor só pode gerar RDO para D-1.");
        }

        EmployeeDTO currentUser = userService.getEmployeeFromUser(userID);

        CompletableFuture<List<BoardedEmployee>> boardedTodayFut = fetchBoardedAsync(today);
        CompletableFuture<List<BoardedEmployee>> boardedD1Fut    = fetchBoardedAsync(targetDate);
//        CompletableFuture<List<MioResponse>> mioStatusToday      = fetchMioStatusAsync(targetDate);
        CompletableFuture.allOf(boardedTodayFut, boardedD1Fut).join();

        List<BoardedEmployee> boardedToday = boardedTodayFut.get();
        List<BoardedEmployee> boardedD1 = boardedD1Fut.get();

        BoardedEmployee currentBoarding = findCurrentBoarding(currentUser, boardedToday, today);
        if (currentBoarding == null) {
            currentBoarding = findBoardingByDateMatch(currentUser, boardedD1, today);
        }
        var contract = currentUser.getContrato().getId().longValue();
        if (currentBoarding == null) {
//            var employee = mioStatusToday.get().stream().filter((e) -> e.matricula().equals(currentUser.getMatricula())).findFirst().orElse(null);
//            if(employee != null) throw new MioFailure(employee, currentUser.getCargo(), "Não foi possível encontrar o colaborador " + currentUser.getName() + " no dia " + today);
            throw new NotFoundEmployee("Não foi possível encontrar o colaborador " + currentUser.getName() + " no dia " + today);
        }

        String platform = currentBoarding.getPlatform();
        PPUEntity activePPU = getActivePPU(platform, contract);
        removeInvalidsEmployeesInAvailableService(activePPU);

        return buildPPUResponse(platform, targetDate, targetDate, currentUser, boardedD1, activePPU);
    }

    public PPUResponse fetchForCoordinator(UUID userID, LocalDate when, CreateRDOCoordinatorRequest request)
            throws IOException {

        LocalDate targetDate = resolveTargetDate(when);
        EmployeeDTO currentUser = userService.getEmployeeFromUser(userID);

        List<BoardedEmployee> allBoardedOnDay = applyDefaultStatus(etl.fetchBoardedEmployeesForDate(
                targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        ));

        String platform = resolvePlatform(request, null);
        PPUEntity activePPU = getActivePPU(platform, request.contratoID());
        removeInvalidsEmployeesInAvailableService(activePPU);
        return buildPPUResponse(platform, targetDate, when, currentUser, allBoardedOnDay, activePPU);
    }

    CompletableFuture<List<BoardedEmployee>> fetchBoardedAsync(LocalDate date) {
        String formatted = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<BoardedEmployee> employees = etl.fetchBoardedEmployeesForDate(formatted, formatted);
                return applyDefaultStatus(employees);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
//    CompletableFuture<List<MioResponse>> fetchMioStatusAsync(LocalDate date) {
//        String formatted = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                return etl.fetchBoardedEmployeesStatus(formatted, formatted);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }

    private String resolvePlatform(CreateRDOCoordinatorRequest request, BoardedEmployee currentBoarding) {
        if (request != null && request.plataforma() != null) {
            return request.plataforma();
        }
        return currentBoarding != null ? currentBoarding.getPlatform() : null;
    }

    PPUResponse buildPPUResponse(String platform, LocalDate targetDate, LocalDate originalWhen,
                                         EmployeeDTO currentUser, List<BoardedEmployee> allBoardedOnDay, PPUEntity activePPU) {

        if (rdo.existsByPlatformAndPpuIdAndDate(platform, activePPU.getId(), originalWhen)) {
            throw new ModuleFailure("Já existe RDO para o dia " + originalWhen);
        }

        Long rdoSequence = rdoService.calculateNextSequenceId(platform, activePPU.getId(), targetDate);

        if (platform != null) {
            if (activePPU.getServices() != null) activePPU.setServices(onlyActive(PlatformFilter.filter(activePPU.getServices(), platform)));
            if (activePPU.getLines() != null) activePPU.setLines(onlyActive(PlatformFilter.filter(activePPU.getLines(), platform)));
            if (activePPU.getEquipments() != null)  activePPU.setEquipments(onlyActive(PlatformFilter.filter(activePPU.getEquipments(), platform)));
            if (activePPU.getAccessoryKits() != null)  activePPU.setAccessoryKits(onlyActive(PlatformFilter.filter(activePPU.getAccessoryKits(), platform)));
            if (activePPU.getAccessoryKits() != null)  activePPU.setSteelCables(onlyActive(PlatformFilter.filter(activePPU.getSteelCables(), platform)));
            if( activePPU.getLines() != null) activePPU.setLines(onlyActive(PlatformFilter.filter(activePPU.getLines(), platform)));
        }

        List<BoardedEmployee> platformBoardings = filterByPlatform(allBoardedOnDay, platform);
        var project = projectService.getbyId(activePPU.getProjectId());
        var ids = project.getFilial().stream().map(FilialHcmEntity::getFilialId).toList();
        List<BoardedEmployee> enrichedBoardings = etl.resolveSimpleEmployeesFromBoardedEmployees(platformBoardings);
        List<BoardedEmployee> filteredByContract = filterByCC(enrichedBoardings, ids);

        assignEmployeesToPPUServices(activePPU, filteredByContract);

        List<BoardedEmployee> assigned = extractAssignedEmployees(activePPU);
        List<BoardedEmployee> unassigned = resolveAnotherBoardedEmployees(assigned, enrichedBoardings);
        List<BoardedEmployee> inLandingDay = extractEmployeesOnDayOff(assigned);

        return ppuResponseMapper.toResponse(
                activePPU,
                platform,
                getEmployeeUseCase.mapperDTO(currentUser),
                unassigned,
                inLandingDay,
                rdoSequence
        );
    }

    private boolean notValid(List<Integer> branchsID, BoardedEmployee emp) {
        var cc = extractLeadingInt(emp.getFilial_HCM()).orElse(null);
        return cc == null || !branchsID.contains(cc);
    }

    public Optional<Integer> extractLeadingInt(String filialHcm) {
        if (filialHcm == null) return Optional.empty();
        Matcher m = Pattern.compile("^\\s*(\\d+)").matcher(filialHcm);
        return m.find() ? Optional.of(Integer.valueOf(m.group(1))) : Optional.empty();
    }


    List<BoardedEmployee> filterByCC(List<BoardedEmployee> all, List<Integer> branchsID) {
        if (all == null || all.isEmpty() || branchsID == null || branchsID.isEmpty()) {
            return Collections.emptyList();
        }

        return all.stream()
                .filter(emp -> !notValid(branchsID, emp))
                .toList();
    }

    static <T extends LinePPU> List<T> onlyActive(List<T> items) {
        return Optional.ofNullable(items)
                .orElse(List.of())
                .stream()
                .filter(i -> !Boolean.FALSE.equals(i.getActive())) // true ou null
                .toList();
    }

    /**
     * Função para atribuir funcionários aos serviços numa PPU.
     * Filtra os funcionários disponíveis na plataforma com base nas posições
     * necessárias para cada serviço.
     *
     * @param ppu                        A PPUEntity cujos serviços serão
     *                                   atualizados.
     * @param availablePlatformEmployees Uma lista de todos os funcionários
     *                                   disponíveis na plataforma.
     */
    void assignEmployeesToPPUServices(PPUEntity ppu, List<BoardedEmployee> availablePlatformEmployees) {
        if (ppu.getServices() == null || availablePlatformEmployees == null)
            return;
        for (ServiceLine serviceItem : ppu.getServices()) {
            if (serviceItem.getPositions() == null || serviceItem.getPositions().isEmpty()) {
                if (Boolean.TRUE.equals(serviceItem.getDisposicao())) continue;
                serviceItem.setEmployees(Collections.emptyList());
                continue;
            }
            List<String> requiredPositionIds = serviceItem.getPositions().stream() .map(ServicePosition::getCbo) .filter(Objects::nonNull) .toList();
            var hcmIds = getPositionService.findHCMCodesByCboCodes(requiredPositionIds);


            List<BoardedEmployee> matchedEmployees = etl.filterSimpleEmployeesByPosition(availablePlatformEmployees,
                    hcmIds);
            serviceItem.setEmployees(matchedEmployees);
        }
    }
    /**
     * Retorna todos os colaboradores embarcados que não foram mencionados em nenhum
     * serviço da PPU.
     *
     * @param mentionedInServices Lista de colaboradores mencionados nos serviços.
     * @param allBoardedEmployees Lista de todos os colaboradores embarcados na plataforma.
     * @return Lista de colaboradores não mencionados nos serviços.
     */
    List<BoardedEmployee> resolveAnotherBoardedEmployees(List<BoardedEmployee> mentionedInServices,
                                                                 List<BoardedEmployee> allBoardedEmployees) {
        if (allBoardedEmployees == null || allBoardedEmployees.isEmpty()) {
            return List.of();
        }
        if (mentionedInServices == null || mentionedInServices.isEmpty()) {
            return allBoardedEmployees;
        }

        Set<BoardedEmployee> mentionedSet = new HashSet<>(mentionedInServices);

        return allBoardedEmployees.stream()
                .filter(employee -> !mentionedSet.contains(employee))
                .toList();
    }

    LocalDate resolveTargetDate(LocalDate when) {
        return (when != null) ? when : LocalDate.now().minusDays(1);
    }

    BoardedEmployee findCurrentBoarding(EmployeeDTO user, List<BoardedEmployee> list, LocalDate date) {
        return list.stream()
                .filter(e -> e.getRegistration().equals(user.getMatricula()))
                .findFirst()
                .orElse(null);
    }

    private BoardedEmployee findBoardingByDateMatch(EmployeeDTO user, List<BoardedEmployee> list, LocalDate date) {
        return list.stream()
                .filter(e -> e.getRegistration().equals(user.getMatricula()))
                .filter(e -> date.equals(e.getBoarding()) || date.equals(e.getLandingForecast()))
                .findFirst()
                .orElse(null);
    }

    private List<BoardedEmployee> applyDefaultStatus(List<BoardedEmployee> employees) {
        if (employees == null || employees.isEmpty()) {
            return List.of();
        }
        employees.forEach(employee -> employee.setStatus(DEFAULT_STATUS));
        return employees;
    }

    PPUEntity getActivePPU(String platform, Long contract) {
        return repository.findByContractIdAndPlatformsInAndStatus(contract, platform, DocumentStatus.ABERTO)
                .orElseThrow(() -> new IllegalArgumentException("PPU não encontrada para o contrato " + contract + " ou não está com status ABERTO."));
    }

    List<BoardedEmployee> filterByPlatform(List<BoardedEmployee> list, String platform) {
        if (list == null) {
            return List.of();
        }
        if (platform == null) {
            return list;
        }
        return list.stream()
                .filter(e -> platform.equals(e.getPlatform()))
                .toList();
    }

    List<BoardedEmployee> extractAssignedEmployees(PPUEntity ppu) {
        return ppu.getServices().stream()
                .filter(s -> s.getEmployees() != null && !s.getDisposicao())
                .flatMap(s -> s.getEmployees().stream())
                .distinct()
                .toList();
    }

    List<BoardedEmployee> extractEmployeesOnDayOff(List<BoardedEmployee> list) {
        return list.stream()
                .filter(e -> "Folga".equalsIgnoreCase(e.getStatus()))
                .toList();
    }


    void removeInvalidsEmployeesInAvailableService(PPUEntity response){
        if (response.getServices() == null) return;

        LocalDate today = LocalDate.now();

        response.getServices().forEach(service -> {
            if (Boolean.TRUE.equals(service.getDisposicao()) && service.getEmployees() != null) {
                List<BoardedEmployee> validEmployees = service.getEmployees().stream()
                        .filter(employee -> {
                            LocalDate landingDate = employee.getAvailableEndDate();
                            return landingDate == null || landingDate.isAfter(today);
                        })
                        .toList();
                service.setEmployees(validEmployees);
            }
        });


        Optional<PPUEntity> ppuOpt = repository.findById(response.getId());
        ppuOpt.ifPresent(ppu -> {
            if (ppu.getServices() != null) {
                ppu.getServices().forEach(service -> {
                    if (Boolean.TRUE.equals(service.getDisposicao())) {
                        response.getServices().stream()
                                .filter(s -> s.getId().equals(service.getId()))
                                .findFirst()
                                .ifPresent(s -> service.setEmployees(s.getEmployees()));
                    }
                });
                repository.save(ppu);
            }
        });
    }
}
