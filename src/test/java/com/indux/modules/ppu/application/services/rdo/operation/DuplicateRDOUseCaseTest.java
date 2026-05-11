package com.indux.modules.ppu.application.services.rdo.operation;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.application.dtos.response.RDOUpdaterResponse;
import com.indux.modules.ppu.application.dtos.response.lines.ServiceLineResponse;
import com.indux.modules.ppu.application.services.ppu.FetchPPUUseCase;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOEmployeeDeparture;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class DuplicateRDOUseCaseTest {

    @Mock
    private RDORepository repository;

    @Mock
    private FetchPPUUseCase fetchPPU;

    @InjectMocks
    private DuplicateRDOUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should filter non boarded employees and mark RDO as duplicated")
    void shouldFilterNonBoardedEmployeesAndMarkAsDuplicated() throws Exception {
        var id = "rdo-1";
        var targetDate = LocalDate.now().minusDays(1);

        var rdo = RDOEntity.builder()
                .id(id)
                .date(LocalDate.now().minusDays(2))
                .statusOP(RDOStatusOP.APPROVED)
                .platform("P-01")
                .services(List.of(
                        RDOServiceEntity.builder().registration("100").build(),
                        RDOServiceEntity.builder().registration("200").build()
                ))
                .employeeDepartures(List.of(
                        RDOEmployeeDeparture.builder().registration("200").build(),
                        RDOEmployeeDeparture.builder().registration("300").build()
                ))
                .duplicated(false)
                .build();

        var ppu = new PPUResponse();
        ppu.setId("id");
        ppu.setBoardedEmployees(List.of(
                BoardedEmployee.builder().registration("100").build(),
                BoardedEmployee.builder().registration("300").build()
        ));
        ppu.setServices(List.of(ServiceLineResponse.builder().employees(List.of()).build()));
        ppu.setInLandingDayEmployees(List.of());
        ppu.setAllowsRDODuplication(true);
        ppu.setCurrentPlatform("P-01");

        when(repository.findById(id)).thenReturn(Optional.of(rdo));
        when(fetchPPU.fetchForSupervisor(any(UUID.class), eq(targetDate))).thenReturn(ppu);

        RDOUpdaterResponse response = useCase.execute(targetDate, id, UUID.randomUUID().toString());

        assertEquals(2, response.rdo().getServices().size());

        Set<String> registrations = response.rdo().getServices().stream()
                .map(RDOServiceEntity::getRegistration)
                .collect(Collectors.toSet());

        assertEquals(Set.of("100", "300"), registrations);
        assertEquals(1, response.rdo().getEmployeeDepartures().size());
        assertEquals("300", response.rdo().getEmployeeDepartures().get(0).getRegistration());
        assertTrue(response.rdo().getDuplicated());
        assertEquals(targetDate, response.rdo().getDate());
    }

    @Test
    @DisplayName("Should throw ModuleFailure when source RDO is already duplicated")
    void shouldThrowWhenSourceRDOIsAlreadyDuplicated() {
        var id = "rdo-2";
        var targetDate = LocalDate.now().minusDays(1);

        var rdo = RDOEntity.builder()
                .id(id)
                .date(targetDate)
                .statusOP(RDOStatusOP.APPROVED)
                .duplicated(true)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(rdo));

        var ex = assertThrows(ModuleFailure.class,
                () -> useCase.execute(targetDate, id, UUID.randomUUID().toString()));

        assertEquals("RDO já duplicado não pode ser duplicado novamente.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw ModuleFailure when target PPU platform differs from source")
    void shouldThrowWhenTargetPpuPlatformDiffersFromSource() throws Exception {
        var id = "rdo-3";
        var targetDate = LocalDate.now().minusDays(1);

        var rdo = RDOEntity.builder()
                .id(id)
                .date(LocalDate.now().minusDays(2))
                .statusOP(RDOStatusOP.APPROVED)
                .platform("P-01")
                .duplicated(false)
                .build();

        var ppu = new PPUResponse();
        ppu.setId("ppu-1");
        ppu.setCurrentPlatform("P-02");
        ppu.setBoardedEmployees(List.of());
        ppu.setInLandingDayEmployees(List.of());
        ppu.setServices(List.of());
        ppu.setAllowsRDODuplication(true);

        when(repository.findById(id)).thenReturn(Optional.of(rdo));
        when(fetchPPU.fetchForSupervisor(any(UUID.class), eq(targetDate))).thenReturn(ppu);

        var ex = assertThrows(ModuleFailure.class,
                () -> useCase.execute(targetDate, id, UUID.randomUUID().toString()));

        assertEquals("Não é permitido duplicar RDO para plataforma diferente da origem.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw ModuleFailure when PPU does not allow duplication")
    void shouldThrowWhenPpuDoesNotAllowDuplication() throws Exception {
        var id = "rdo-4";
        var targetDate = LocalDate.now().minusDays(1);

        var rdo = RDOEntity.builder()
                .id(id)
                .date(LocalDate.now().minusDays(2))
                .statusOP(RDOStatusOP.APPROVED)
                .platform("P-01")
                .duplicated(false)
                .build();

        var ppu = new PPUResponse();
        ppu.setId("ppu-1");
        ppu.setCurrentPlatform("P-01");
        ppu.setAllowsRDODuplication(false);
        ppu.setBoardedEmployees(List.of());
        ppu.setInLandingDayEmployees(List.of());
        ppu.setServices(List.of());

        when(repository.findById(id)).thenReturn(Optional.of(rdo));
        when(fetchPPU.fetchForSupervisor(any(UUID.class), eq(targetDate))).thenReturn(ppu);

        var ex = assertThrows(ModuleFailure.class,
                () -> useCase.execute(targetDate, id, UUID.randomUUID().toString()));

        assertEquals("PPU alvo não autoriza duplicação de RDO.", ex.getMessage());
    }
}