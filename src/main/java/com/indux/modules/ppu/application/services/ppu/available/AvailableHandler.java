package com.indux.modules.ppu.application.services.ppu.available;

import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.response.AvailableServiceDTO;
import com.indux.modules.ppu.domain.entities.ppu.AvailableType;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.infra.mapper.AvailableServiceResponseMapper;
import com.indux.modules.ppu.infra.mapper.BoardedEmployeeMapper;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AvailableHandler {
    private final PPURepository ppuRepository;
    private final AvailableServiceResponseMapper mapper;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final BoardedEmployeeMapper boardedMapper;


    public AvailableHandler(PPURepository ppuRepository, AvailableServiceResponseMapper mapper, GetEmployeeUseCase getEmployeeUseCase, BoardedEmployeeMapper boardedMapper) {
        this.ppuRepository = ppuRepository;
        this.mapper = mapper;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.boardedMapper = boardedMapper;
    }

    public AvailableServiceResponse getAvailableServices(String ppuID) {
        var ppu = ppuRepository.findById(ppuID).orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada no sistema."));
        var services = ppu.getServices();
        var availableServices = services.stream()
                .filter(ServiceLine::getDisposicao)
                .map(mapper::toResponse)
                .toList();
        return new AvailableServiceResponse(availableServices, ppu.getPlatforms(), ppu.getAvailableType());
    }

    public void addEmployeeAvailable(List<AvailableEmployee> availableEmployees, String serviceID, String ppuID) {
        var ppu = ppuRepository.findById(ppuID)
                .orElseThrow(() -> new ModuleFailure("PPU não encontrada no sistema."));
        var boardedEmployees = getBoardedEmployees(availableEmployees, ppu.getAvailableType());
        var service = ppu.getServices().stream()
                .filter(s -> Objects.equals(s.getId(), serviceID))
                .findFirst()
                .orElseThrow(() -> new ModuleFailure("Serviço com ID " + serviceID + " não encontrado na PPU."));

        if (!service.getDisposicao()) throw new ModuleFailure("Serviço não está marcado como de disposição.");

        service.setEmployees(boardedEmployees);
        ppuRepository.save(ppu);
    }

    @NotNull
    private List<BoardedEmployee> getBoardedEmployees(List<AvailableEmployee> availableEmployees, List<AvailableType> types) {
        var registrations = availableEmployees.stream().map(AvailableEmployee::matricula).toList();
        var employeesDTOs = getEmployeeUseCase.findByRegistrations(registrations);

        if (employeesDTOs.size() != availableEmployees.size()) {
            throw new ModuleFailure("Um ou mais colaboradores não foram encontrados na base de dados.");
        }

        var platformByRegistration = availableEmployees.stream()
                .collect(Collectors.toMap(AvailableEmployee::matricula, AvailableEmployee::plataformas));

        return employeesDTOs.stream().flatMap(dto -> {
                    var platforms = platformByRegistration.get(dto.getRegistration());
                    return platforms.stream().map(platform -> {
                        var boardedEmployee = boardedMapper.fromSimpleEmployeeDTO(dto);
                        var availableInfo = availableEmployees.stream().filter(registration -> registration.matricula.equals(boardedEmployee.getRegistration())).findFirst();
                        availableInfo.ifPresent(availableEmployee -> {
                            if (availableEmployee.type() != null) {
                                var currentType = types.stream().filter(e -> e.getName().equals(availableEmployee.type())).findFirst();
                                if (currentType.isPresent()) {
                                    boardedEmployee.setAvailableEndDate(LocalDate.now().plusDays(currentType.get().getQuantityDays()));
                                    boardedEmployee.setAvailableType(currentType.get().getName());
                                }
                            }
                            if (availableEmployee.fimDisposicao() != null) {
                                boardedEmployee.setAvailableEndDate(availableEmployee.fimDisposicao());
                            }
                            boardedEmployee.setAvailableStartDate(LocalDate.now());
                        });
                        boardedEmployee.setPlatform(platform);
                        return boardedEmployee;
                    });
                })
                .toList();
    }

    public record AvailableEmployee(String matricula, List<String> plataformas, @Nullable LocalDate fimDisposicao,
                                    @Nullable String type) {
    }

    public record AvailableServiceResponse(List<AvailableServiceDTO> servicos, List<String> plataformas, List<AvailableType> tipos) {
    }
}
