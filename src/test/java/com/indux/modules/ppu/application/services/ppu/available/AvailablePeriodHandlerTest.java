package com.indux.modules.ppu.application.services.ppu.available;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.service.user.UserService;
import com.indux.modules.clients.domain.repository.ClientRepository;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.strategy.LineStrategiesApplier;
import com.indux.modules.ppu.infra.mapper.BoardedEmployeeMapper;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import com.indux.modules.ppu.presentation.dtos.AvailablePeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvailablePeriodHandlerTest {

    @Mock
    private RDORepository rdoRepository;
    @Mock
    private PPURepository ppuRepository;
    @Mock
    private BoardedEmployeeMapper boardedMapper;
    @Mock
    private GetEmployeeUseCase getEmployeeUseCase;
    @Mock
    private LineStrategiesApplier lineApplier;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private UserService userService;

    private AvailablePeriodHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new AvailablePeriodHandler(
                rdoRepository,
                ppuRepository,
                boardedMapper,
                getEmployeeUseCase,
                lineApplier,
                clientRepository,
                userService);
    }

    @Test
    void addMultipleAvailablePeriodsShouldCreateApprovedRdoWhenPpuHasNoSupervisorOnBoard() {
        var request = request();
        mockCreationDependencies(ppu(false), request);

        handler.addMultipleAvailablePeriods(request, UUID.randomUUID().toString());

        var saved = captureSavedRdo();
        assertEquals(RDOStatusOP.APPROVED, saved.getStatusOP());
    }

    @Test
    void addMultipleAvailablePeriodsShouldCreatePendingRdoWhenPpuHasSupervisorOnBoard() {
        var request = request();
        mockCreationDependencies(ppu(true), request);

        handler.addMultipleAvailablePeriods(request, UUID.randomUUID().toString());

        var saved = captureSavedRdo();
        assertEquals(RDOStatusOP.PENDING, saved.getStatusOP());
    }

    private AvailablePeriod request() {
        return new AvailablePeriod(
                List.of("123"),
                LocalDate.of(2025, 7, 16),
                LocalDate.of(2025, 7, 16),
                "PLAT-1",
                "service-1",
                "ppu-1",
                null);
    }

    private PPUEntity ppu(boolean hasSupervisorOnBoard) {
        return PPUEntity.builder()
                .id("ppu-1")
                .platforms(List.of("PLAT-1"))
                .clientId(10L)
                .contract(Map.of("codeSap", "400400400"))
                .regionalNome("Regional")
                .regionalId(1L)
                .projectId(20L)
                .hasSupervisorOnBoard(hasSupervisorOnBoard)
                .services(List.of(ServiceLine.builder()
                        .id("service-1")
                        .genericNumber("1.1")
                        .name("Servico a disposicao")
                        .disposicao(true)
                        .build()))
                .build();
    }

    private void mockCreationDependencies(PPUEntity ppu, AvailablePeriod request) {
        var creator = new EmployeeDTO();
        creator.setMatricula("999");
        creator.setName("Creator");

        var employee = new SimpleEmployeeDTO(
                "123",
                "Employee",
                "cargo-1",
                "Cargo",
                "SISPAT",
                Map.of());

        var boarded = BoardedEmployee.builder()
                .registration("123")
                .name("Employee")
                .position("cargo-1")
                .positionName("Cargo")
                .build();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(userService.getEmployeeFromUser(org.mockito.ArgumentMatchers.any(UUID.class))).thenReturn(creator);
        when(getEmployeeUseCase.findByRegistrations(request.registrations())).thenReturn(List.of(employee));
        when(boardedMapper.fromSimpleEmployeeDTOList(List.of(employee))).thenReturn(List.of(boarded));
        when(rdoRepository.findAllByPlatformAndPpuIdAndDateBetween(
                request.platform(),
                request.ppuId(),
                request.start(),
                request.end())).thenReturn(List.of());
        when(rdoRepository.findTopByPlatformAndPpuIdAndDateBeforeOrderByDateDesc(
                request.platform(),
                request.ppuId(),
                request.start())).thenReturn(Optional.empty());
        when(clientRepository.findById(10L)).thenReturn(Optional.empty());
    }

    @SuppressWarnings("unchecked")
    private RDOEntity captureSavedRdo() {
        ArgumentCaptor<List<RDOEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(rdoRepository).saveAll(captor.capture());
        return captor.getValue().getFirst();
    }
}
