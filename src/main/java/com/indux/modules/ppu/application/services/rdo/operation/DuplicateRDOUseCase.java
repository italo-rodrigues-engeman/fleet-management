package com.indux.modules.ppu.application.services.rdo.operation;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.application.dtos.response.lines.ServiceLineResponse;
import com.indux.modules.ppu.application.dtos.requests.CreateRDOCoordinatorRequest;
import com.indux.modules.ppu.application.dtos.response.RDOUpdaterResponse;
import com.indux.modules.ppu.application.services.ppu.FetchPPUUseCase;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.springframework.stereotype.Component;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DuplicateRDOUseCase {
    private final RDORepository repository;
    private final FetchPPUUseCase fetchPPU;

    public DuplicateRDOUseCase(RDORepository repository, FetchPPUUseCase fetchPPU) {
        this.repository = repository;
        this.fetchPPU = fetchPPU;
    }

    public RDOUpdaterResponse execute(LocalDate day, String id, String userID) throws IOException, ExecutionException, InterruptedException {
        var rdo = repository.findById(id).orElseThrow(()-> new ModuleNotFoundFailure("RDO não encontrado no sistema."));
        validateSourceRDO(rdo);
        var ppu = fetchPPU.fetchForSupervisor(UUID.fromString(userID), day);
        validateTargetPPU(rdo, ppu);
        return new RDOUpdaterResponse(mergeEmployees(rdo, ppu, day), ppu);
    }

    public RDOUpdaterResponse execute(CreateRDOCoordinatorRequest request, String id, String userID) throws IOException, ExecutionException, InterruptedException {
        var rdo = repository.findById(id).orElseThrow(()-> new ModuleNotFoundFailure("RDO não encontrado no sistema."));
        validateSourceRDO(rdo);
        var ppu = fetchPPU.fetchForCoordinator(UUID.fromString(userID), request.data(), request);
        validateTargetPPU(rdo, ppu);
        return new RDOUpdaterResponse(mergeEmployees(rdo, ppu, request.data()), ppu);
    }
    private void validateSourceRDO(RDOEntity rdo) {
        if (Boolean.TRUE.equals(rdo.getDuplicated())) {
            throw new ModuleFailure("RDO já duplicado não pode ser duplicado novamente.");
        }
        if (rdo.getStatusOP() != RDOStatusOP.APPROVED) {
            throw new ModuleFailure("Apenas RDO aprovado pode ser duplicado.");
        }
        if (rdo.getDate() == null || !rdo.getDate().isBefore(LocalDate.now())) {
            throw new ModuleFailure("Somente RDO de data anterior pode ser duplicado.");
        }
    }

    private void validateTargetPPU(RDOEntity source, PPUResponse ppu) {
        if (ppu == null || ppu.getId() == null) {
            throw new ModuleFailure("PPU alvo inválida para duplicação.");
        }
        if (ppu.getCurrentPlatform() == null || ppu.getCurrentPlatform().isBlank()) {
            throw new ModuleFailure("PPU alvo sem plataforma válida para duplicação.");
        }
        if (!Boolean.TRUE.equals(ppu.getAllowsRDODuplication())) {
            throw new ModuleFailure("PPU alvo não autoriza duplicação de RDO.");
        }
        if (!Objects.equals(source.getPlatform(), ppu.getCurrentPlatform())) {
            throw new ModuleFailure("Não é permitido duplicar RDO para plataforma diferente da origem.");
        }
    }

    private RDOEntity mergeEmployees(RDOEntity source, PPUResponse ppu, LocalDate targetDate) {
        RDOEntity copy = new RDOEntity();
        BeanUtils.copyProperties(source, copy);
        copy.setDate(targetDate);

        Map<String, BoardedEmployee> mioEmployees = extractMioEmployees(ppu);
        Set<String> mioRegistrations = mioEmployees.keySet();
        Map<String, ServiceLineResponse> serviceByRegistration = mapServiceByRegistration(ppu);

        List<RDOServiceEntity> existingServices = copy.getServices() != null ? copy.getServices() : List.of();
        Set<String> existingRegistrations = existingServices.stream()
                .map(RDOServiceEntity::getRegistration)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<RDOServiceEntity> merged = new ArrayList<>(existingServices.stream()
                .filter(service -> mioRegistrations.contains(service.getRegistration()))
                .toList());

        for (String registration : mioRegistrations) {
            if (existingRegistrations.contains(registration)) {
                continue;
            }
            BoardedEmployee employee = mioEmployees.get(registration);
            ServiceLineResponse service = serviceByRegistration.get(registration);
            merged.add(newServiceFromMio(employee, service));
        }
        copy.setServices(merged);

        if (copy.getEmployeeDepartures() != null) {
            copy.setEmployeeDepartures(copy.getEmployeeDepartures().stream()
                    .filter(employee -> mioRegistrations.contains(employee.getRegistration()))
                    .toList());
        }
        copy.setDuplicated(true);
        return copy;
    }

    private Map<String, BoardedEmployee> extractMioEmployees(PPUResponse ppu) {
        List<BoardedEmployee> boarded = ppu.getBoardedEmployees() != null ? ppu.getBoardedEmployees() : List.of();
        List<BoardedEmployee> inLanding = ppu.getInLandingDayEmployees() != null ? ppu.getInLandingDayEmployees() : List.of();
        List<ServiceLineResponse> services = ppu.getServices() != null ? ppu.getServices() : List.of();

        return Stream.of(
                        boarded.stream(),
                        inLanding.stream(),
                        services.stream()
                                .map(ServiceLineResponse::getEmployees)
                                .filter(list -> list != null && !list.isEmpty())
                                .flatMap(List::stream)
                )
                .flatMap(s -> s)
                .filter(Objects::nonNull)
                .filter(e -> e.getRegistration() != null && !e.getRegistration().isBlank())
                .collect(Collectors.toMap(
                        BoardedEmployee::getRegistration,
                        e -> e,
                        (current, incoming) -> current
                ));
    }

    private Map<String, ServiceLineResponse> mapServiceByRegistration(PPUResponse ppu) {
        List<ServiceLineResponse> services = ppu.getServices() != null ? ppu.getServices() : List.of();
        Map<String, ServiceLineResponse> map = new HashMap<>();
        for (ServiceLineResponse service : services) {
            if (service.getEmployees() == null) continue;
            for (BoardedEmployee employee : service.getEmployees()) {
                if (employee == null || employee.getRegistration() == null) continue;
                map.putIfAbsent(employee.getRegistration(), service);
            }
        }
        return map;
    }

    private RDOServiceEntity newServiceFromMio(BoardedEmployee employee, ServiceLineResponse service) {
        return RDOServiceEntity.builder()
                .id(UUID.randomUUID().toString())
                .serviceID(service != null ? service.getId() : null)
                .serviceName(service != null ? service.getName() : null)
                .serviceNumber(service != null ? service.getGenericNumber() : null)
                .registration(employee.getRegistration())
                .name(employee.getName())
                .cargoID(employee.getPosition())
                .cargoNome(employee.getPositionName())
                .sispat(employee.getSispat())
                .present(true)
                .schedule(new ShiftSchedule())
                .dayType("rotina")
                .flagman(false)
                .overtimes(List.of())
                .nightShiftPremium(Duration.ZERO)
                .normalHours(Duration.ZERO)
                .hourTotais(Duration.ZERO)
                .overtimeHourTotais(Duration.ZERO)
                .statusEmployee(employee.getStatus())
                .doubleFold(false)
                .disposicao(service != null && Boolean.TRUE.equals(service.getDisposicao()))
                .build();
    }

    private RDOEntity buildDuplicatedEntity(RDOEntity source, PPUResponse ppu, LocalDate targetDate) {
        source.setId(null);
        source.setDate(targetDate);
        source.setCreatedAt(LocalDateTime.now());
        source.setPpuId(ppu.getId());
        source.setPlatform(ppu.getCurrentPlatform());
        source.setSequentialId(ppu.getRdoCodeSequence());
        source.setContract(ppu.getContract());
        source.setProjectId(ppu.getProjectId());
        source.setRegionalId(ppu.getRegionalId());
        source.setRegionalNome(ppu.getRegionalNome());
        source.setStatusDP(RDOStatusDP.PENDING);
        source.setStatusOP(Boolean.FALSE.equals(ppu.getHasSupervisorOnBoard())
                ? RDOStatusOP.APPROVED
                : RDOStatusOP.PENDING);
        source.setCompetence("-");
        source.setAttachments(List.of());
        source.setLoggers(List.of());
        source.setDivergences(List.of());
        return source;
    }
}
