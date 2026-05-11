package com.indux.modules.ppu.application.services.ppu;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.model.employee.ContractProject;
import com.indux.core.domain.model.employee.EmployeePosition;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.service.generic.GetPositionService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.modules.ppu.application.services.fixtures.PPUFixture;
import com.indux.modules.ppu.application.services.rdo.RDOService;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.exceptions.MioFailure;
import com.indux.modules.ppu.infra.mapper.PPUResponseMapper;
import com.indux.modules.ppu.infra.mio.EmployeeBoardingETL;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FetchPPUUseCaseTest {
    @Mock
    private EmployeeBoardingETL mockEtl;
    @Mock
    private UserService mockUserService;
    @Mock
    private PPURepository mockRepository;
    @Mock
    private RDORepository mockRdoRepository;
    @Mock
    private RDOService mockRdoService;
    @Mock
    private GetEmployeeUseCase mockGetEmployee;
    @Mock
    private PPUResponseMapper mockPpuResponseMapper;

    @Mock private GetPositionService<EmployeePosition> mockPositionService;

    @InjectMocks
    private FetchPPUUseCase fetch;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("Should filter by contract")
    void filterByContract() {
        var list = List.of(FetchPPUFixtures.fakeBoardedEmployeeP02(), FetchPPUFixtures.fakeBoardedEmployeeP02DayOff());
        var contract = List.of(123);

        var response = fetch.filterByCC(list, contract);
        assertEquals(List.of(FetchPPUFixtures.fakeBoardedEmployeeP02()), response);
        assertEquals(1, response.size());
    }

    @Test
    @DisplayName("Should return empty list when try filter by contract using null itens")
    void filterByContractEmpty() {
        var response = fetch.filterByCC(null, null);
        assertEquals(List.of(), response);
        assertEquals(0, response.size());
    }

    @Test
    @DisplayName("Should return empty list when try filter by contract")
    void filterByContractWithoutItens() {
        var response = fetch.filterByCC(List.of(), List.of(123));
        assertEquals(List.of(), response);
        assertEquals(0, response.size());
    }

    @Test
    @DisplayName("Should assign boarded employees in services.")
    void assignEmployeesToPPUServices() {
        var ppu = PPUFixture.fakePPUEntityFunction();
        ppu.getServices().add(PPUFixture.fakeServiceLineWithPositions);
        var boardedEmployees = List.of(
                FetchPPUFixtures.fakeBoardedEmployee(),
                FetchPPUFixtures.fakeBoardedEmployeeP02()
        );

        when(mockPositionService.findHCMCodesByCboCodes(List.of("2220")))
                .thenReturn(List.of("Cargo_ID"));

        when(mockEtl.filterSimpleEmployeesByPosition(boardedEmployees, List.of("Cargo_ID")))
                .thenReturn(boardedEmployees);

        fetch.assignEmployeesToPPUServices(ppu, boardedEmployees);

        assertEquals(boardedEmployees, ppu.getServices().get(1).getEmployees());
    }

    @Test
    @DisplayName("Should don't assign boarded employees in services.")
    void doNotAssignEmployeesToPPUServices() {
        var ppu = PPUFixture.fakePPUEntityFunction();
        fetch.assignEmployeesToPPUServices(ppu, null);

        assertNull(ppu.getServices().getFirst().getEmployees());
    }

    @Test
    @DisplayName("Should filter by contract and return empty list")
    void filterByContractEmptyList() {
        var list = List.of(FetchPPUFixtures.fakeBoardedEmployee(), FetchPPUFixtures.fakeBoardedEmployeeWithDayOff());
        var contract = List.of(123);

        var response = fetch.filterByCC(list, contract);
        assertEquals(List.of(), response);
        assertEquals(0, response.size());
    }

    @Test
    @DisplayName("Should return boarded employees without service")
    void resolveAnotherBoardedEmployees() {
        var listWithAllEmployeesBoarded = List.of(FetchPPUFixtures.fakeBoardedEmployee(), FetchPPUFixtures.fakeBoardedEmployeeWithDayOff(), FetchPPUFixtures.fakeBoardedEmployee());
        var listWithMentionedInServices = List.of(FetchPPUFixtures.fakeBoardedEmployee());

        var response = fetch.resolveAnotherBoardedEmployees(listWithMentionedInServices, listWithAllEmployeesBoarded);
        assertEquals(List.of(FetchPPUFixtures.fakeBoardedEmployeeWithDayOff()), response);
        assertEquals(1, response.size());
    }

    @Test
    @DisplayName("Should return empty when there are no boarded employees")
    void shouldReturnEmptyListInResolveAnotherEmployees() {
        List<BoardedEmployee> listWithAllEmployeesBoarded = Collections.emptyList();
        var listWithMentionedInServices = List.of(FetchPPUFixtures.fakeBoardedEmployee());

        var response = fetch.resolveAnotherBoardedEmployees(listWithMentionedInServices, listWithAllEmployeesBoarded);
        assertEquals(List.of(), response);
        assertEquals(0, response.size());
    }

    @Test
    @DisplayName("Should return all employees when mentioned list is null")
    void shouldReturnEmptyListInResolveAnotherEmployeesWhenMentionedInServicesNotExist() {
        var listWithAllEmployeesBoarded = List.of(FetchPPUFixtures.fakeBoardedEmployee(), FetchPPUFixtures.fakeBoardedEmployeeWithDayOff(), FetchPPUFixtures.fakeBoardedEmployee());

        var response = fetch.resolveAnotherBoardedEmployees(null, listWithAllEmployeesBoarded);
        assertEquals(listWithAllEmployeesBoarded, response);
        assertEquals(3, response.size());
    }

    @Test
    @DisplayName("Should use yesterday (D-1) when date is null")
    void resolveTargetDate() {
        assertEquals(LocalDate.now().minusDays(1), fetch.resolveTargetDate(null));
    }

    @Test
    @DisplayName("Should find current boarding by registration")
    void findCurrentBoarding() {
        var employee = EmployeeDTO.builder().matricula("matricula-123").build();
        var listBoarded = List.of(FetchPPUFixtures.fakeBoardedEmployee(), FetchPPUFixtures.fakeBoardedEmployeeWithDayOff());
        LocalDate date = LocalDate.of(2019, 9, 19);

        var response = fetch.findCurrentBoarding(employee, listBoarded, date);

        assertEquals(FetchPPUFixtures.fakeBoardedEmployee(), response);
    }

    @Test
    @DisplayName("Should throw when employee is not boarded with detailed info")
    void doNotFindCurrentBoarding() {
        var employee = EmployeeDTO.builder()
                .matricula("matricula-1239")
                .name("Funcionário Teste")
                .cargo("Função Teste")
                .build();
        var listBoarded = List.of(FetchPPUFixtures.fakeBoardedEmployee(), FetchPPUFixtures.fakeBoardedEmployeeWithDayOff());
        LocalDate date = LocalDate.of(2019, 9, 19);
        var result = fetch.findCurrentBoarding(employee, listBoarded, date);
        assertNull(result);
    }

    @Test
    @DisplayName("Should throw when employee is not boarded in fetchForSupervisor with detailed info")
    void fetchForSupervisorEmployeeNotBoarded() {
        FetchPPUUseCase spyFetch = spy(fetch);
        var employee = EmployeeDTO.builder()
                .matricula("matricula-1239")
                .name("Funcionário Teste")
                .cargo("Função Teste")
                .contrato(ContractProject.builder().id(25).build())
                .build();
        var listBoarded = List.of();
        LocalDate date = LocalDate.now();

        when(mockUserService.getEmployeeFromUser(any())).thenReturn(employee);
        doReturn(CompletableFuture.completedFuture(listBoarded))
                .when(spyFetch).fetchBoardedAsync(date);
//        doReturn(CompletableFuture.completedFuture(listBoarded))
//                .when(spyFetch).fetchMioStatusAsync(date);
        var exception = assertThrows(NotFoundEmployee.class, () -> spyFetch.fetchForSupervisor(UUID.randomUUID(), null));
        assertThat(exception.getMessage()).contains("Não foi possível encontrar o colaborador");
    }

    @Test
    @DisplayName("Should return detailed error message when findCurrentBoarding fails")
    void findCurrentBoardingShouldReturnDetailedErrorMessage() {
        var employee = EmployeeDTO.builder()
                .matricula("matricula-1239")
                .name("Funcionário Teste")
                .cargo("Função Teste")
                .build();
        List<BoardedEmployee> listBoarded = List.of(); // Empty list to trigger exception
        LocalDate date = LocalDate.of(2019, 9, 19);
        var result = fetch.findCurrentBoarding(employee, listBoarded, date);
        assertNull(result);
    }

    @Test
    @DisplayName("Should return detailed error message when fetchForSupervisor fails due to employee not boarded")
    void fetchForSupervisorShouldReturnDetailedErrorMessage() throws ExecutionException, InterruptedException {
        FetchPPUUseCase spyFetch = spy(fetch);
        var employee = EmployeeDTO.builder()
                .matricula("matricula-1239")
                .name("Funcionário Teste")
                .cargo("Função Teste")
                .contrato(ContractProject.builder().id(25).build())
                .build();
        var listBoarded = List.of();

        when(mockUserService.getEmployeeFromUser(any())).thenReturn(employee);
        doReturn(CompletableFuture.completedFuture(listBoarded))
                .when(spyFetch).fetchBoardedAsync(any());
//        doReturn(CompletableFuture.completedFuture(listBoarded))
//                .when(spyFetch).fetchMioStatusAsync(any());

        var exception = assertThrows(NotFoundEmployee.class,
                () -> spyFetch.fetchForSupervisor(UUID.randomUUID(), null));
        assertThat(exception.getMessage()).contains("Não foi possível encontrar o colaborador");
    }

    @Test
    @DisplayName("Should find active PPU by contract and platform")
    void getActivePPU() {
        var ppu = PPUFixture.fakePPUEntityFunction();
        var platform = "P-01";
        var contract = List.of(2L);
        when(mockRepository.findByContractIdAndPlatformsInAndStatus(contract.get(0), platform, DocumentStatus.ABERTO)).thenReturn(Optional.of(ppu));

        var response = fetch.getActivePPU(platform, contract.getFirst());
        assertEquals(ppu, response);
    }

    @Test
    @DisplayName("Should throw when active PPU is not found")
    void getActivePPUError() {
        var ppu = PPUFixture.fakePPUEntityFunction();
        var platform = "P-02";
        var contract = 2L;
        var exception = assertThrows(IllegalArgumentException.class, () -> fetch.getActivePPU(platform, contract));
        assertThat(exception.getMessage()).isEqualTo("PPU não encontrada para o contrato " + contract + " ou não está com status ABERTO.");
    }

    @Test
    @DisplayName("Should filter employees by platform")
    void filterByPlatform() {
        var list = List.of(FetchPPUFixtures.fakeBoardedEmployee(), FetchPPUFixtures.fakeBoardedEmployeeP02(), FetchPPUFixtures.fakeBoardedEmployeeP02DayOff());
        var platform = "P-01";
        var response = fetch.filterByPlatform(list, platform);

        assertEquals(1, response.size());
        assertFalse(response.contains(FetchPPUFixtures.fakeBoardedEmployeeP02()));
    }

    @Test
    @DisplayName("Should return empty assigned employees when none")
    void extractAssignedEmployees() {
        var ppu = PPUFixture.fakePPUEntityFunction();
        var response = fetch.extractAssignedEmployees(ppu);

        assertEquals(List.of(), response);
    }

    @Test
    @DisplayName("Should return only employees on day off")
    void extractEmployeesOnDayOff() {
        var list = List.of(FetchPPUFixtures.fakeBoardedEmployee(), FetchPPUFixtures.fakeBoardedEmployeeWithDayOff());

        var response = fetch.extractEmployeesOnDayOff(list);
        assertEquals(List.of(FetchPPUFixtures.fakeBoardedEmployeeWithDayOff()), response);
    }

    @Test
    @DisplayName("Should return empty when no employees on day off")
    void extractEmployeesOnDayOffEmpty() {
        List<BoardedEmployee> list = List.of();

        var response = fetch.extractEmployeesOnDayOff(list);
        assertEquals(List.of(), response);
    }

    @Test
    @DisplayName("Should remove employee when availableEndDate is before today")
    void resolveAvailableEmployee() {
        var employee = FetchPPUFixtures.fakeBoardedAvailableEmployee();
        var service = ServiceLine.builder().disposicao(true).employees(List.of(employee)).build();
        var ppu = PPUEntity.builder().services(List.of(service)).build();

        when(mockRepository.save(ppu)).thenReturn(ppu);
        fetch.removeInvalidsEmployeesInAvailableService(ppu);
        assertEquals(0, ppu.getServices().getFirst().getEmployees().size());
    }

    @Test
    @DisplayName("Should keep employee when availableEndDate is after today")
    void resolveAvailableEmployee2() {
        var employee = FetchPPUFixtures.fakeBoardedAvailableEmployee();
        employee.setAvailableEndDate(LocalDate.now().plusDays(1));
        var service = ServiceLine.builder().disposicao(true).employees(List.of(employee)).build();
        var ppu = PPUEntity.builder().services(List.of(service)).build();

        when(mockRepository.save(ppu)).thenReturn(ppu);
        fetch.removeInvalidsEmployeesInAvailableService(ppu);
        assertEquals(1, ppu.getServices().getFirst().getEmployees().size());
    }
}