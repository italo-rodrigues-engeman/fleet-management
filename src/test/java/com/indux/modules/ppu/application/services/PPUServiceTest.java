//package com.indux.modules.ppu.application.services;
//
//import com.indux.core.application.dto.generic.EmployeeDTO;
//import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
//import com.indux.core.application.service.employee.GetEmployeeUseCase;
//import com.indux.core.domain.model.employee.ContractProject;
//import com.indux.core.domain.model.modules.form.DocumentStatus;
//import com.indux.core.domain.repository.generic.ContractProjectRepository;
//import com.indux.core.domain.service.user.UserService;
//import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
//import com.indux.core.infra.exception.user.NotFoundEmployee;
//import com.indux.modules.ppu.application.dtos.PPUResponse;
//import com.indux.modules.ppu.application.dtos.requests.PPURequest;
//import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
//import com.indux.modules.ppu.domain.entities.jpa.DatabaseSequencePPU;
//import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
//import com.indux.modules.ppu.domain.repositories.jpa.DatabaseSequencePPURepository;
//import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
//import com.indux.modules.ppu.infra.mio.EmployeeBoardingETL;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//class PPUServiceTest {
//    @Mock
//    private PPURepository repository;
//
//    @Mock
//    private DatabaseSequencePPURepository databaseSequence;
//
//    @Mock
//    private ContractProjectRepository contractProjectRepository;
//
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private GetEmployeeUseCase getEmployeeUseCase;
//
//    @Mock
//    private EmployeeBoardingETL etl;
//
//    @InjectMocks
//    private PPUService ppuService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    @DisplayName("Should return correct page when finding all PPUs")
//    void findAll_ShouldReturnPageOfPPUEntity() {
//        Pageable pageable = Pageable.unpaged();
//        List<PPUEntity> ppuEntities = Collections.singletonList(new PPUEntity());
//        Page<PPUEntity> page = new PageImpl<>(ppuEntities);
//        when(repository.findAll(pageable)).thenReturn(page);
//
//        Page<PPUEntity> result = ppuService.findAll(pageable);
//
//        assertEquals(page, result);
//        verify(repository).findAll(pageable);
//    }
//
//    @Test
//    @DisplayName("Should return PPU when finding by ID")
//    void findById_ShouldReturnOptionalOfPPUEntity() {
//        String id = "1";
//        PPUEntity ppuEntity = new PPUEntity();
//        when(repository.findById(id)).thenReturn(Optional.of(ppuEntity));
//
//        Optional<PPUEntity> result = ppuService.findById(id);
//
//        assertTrue(result.isPresent());
//        assertEquals(ppuEntity, result.get());
//        verify(repository).findById(id);
//    }
//
//    @Test
//    @DisplayName("Should return saved PPU when saving")
//    void save_ShouldReturnSavedPPUEntity() {
//        PPUEntity ppuEntity = new PPUEntity();
//        when(repository.save(ppuEntity)).thenReturn(ppuEntity);
//
//        PPUEntity result = ppuService.save(ppuEntity);
//
//        assertEquals(ppuEntity, result);
//        verify(repository).save(ppuEntity);
//    }
//
//    @Test
//    @DisplayName("Should delete PPU when deleting by ID")
//    void delete_ShouldDeletePPUEntityById() {
//        String id = "1L";
//        doNothing().when(repository).deleteById(id);
//
//        ppuService.delete(id);
//
//        verify(repository).deleteById(id);
//    }
//
//    @Test
//    @DisplayName("Should return generated code when creating PPU with valid record")
//    void createPPU_ShouldCreateAndReturnPPUCodeId() {
//        PPURequest dto = PPUServiceTestFixtures.createPPURecord();
//        ContractProject contract = PPUServiceTestFixtures.createContractProject();
//        List<SimpleEmployeeDTO> dispositionEmployees = PPUServiceTestFixtures.createDispositionEmployees();
//        DatabaseSequencePPU ppuSequence = PPUServiceTestFixtures.createPPUSequence();
//        PPUEntity newPPU = PPUServiceTestFixtures.createPPUEntity();
//
//        when(contractProjectRepository.findById(dto.contratoID())).thenReturn(Optional.of(contract));
//        when(getEmployeeUseCase.findByRegistrations(dto.disposicao())).thenReturn(dispositionEmployees);
//        when(databaseSequence.save(any(DatabaseSequencePPU.class))).thenReturn(ppuSequence);
//        when(repository.save(any(PPUEntity.class))).thenReturn(newPPU);
//
//        Long result = ppuService.createPPU(dto);
//
//        assertEquals(1L, result);
//        verify(contractProjectRepository).findById(dto.contratoID());
//        verify(getEmployeeUseCase).findByRegistrations(dto.disposicao());
//        verify(databaseSequence, times(2)).save(any(DatabaseSequencePPU.class));
//        verify(repository).save(any(PPUEntity.class));
//    }
//
//    @Test
//    @DisplayName("Should throw ModuleNotFoundFailure when creating PPU with non-existent contract")
//    void createPPU_ShouldThrowModuleNotFoundFailure_WhenContractNotFound() {
//        PPURequest dto = PPUServiceTestFixtures.createPPURecord();
//        when(contractProjectRepository.findById(dto.contratoID())).thenReturn(Optional.empty());
//
//        assertThrows(ModuleNotFoundFailure.class, () -> ppuService.createPPU(dto));
//        verify(contractProjectRepository).findById(dto.contratoID());
//        verifyNoInteractions(getEmployeeUseCase, databaseSequence, repository);
//    }
//
//    @Test
//    @DisplayName("Should return PPU when fetching current PPU for boarded user")
//    void fetchCurrentPPU_ShouldReturnRecordPPU() throws IOException {
//        UUID userId = UUID.randomUUID();
//        LocalDate when = LocalDate.now().minusDays(1);
//        EmployeeDTO currentUser = PPUServiceTestFixtures.createEmployeeDTO();
//        BoardedEmployee boardedEmployee = PPUServiceTestFixtures.createBoardedEmployee();
//        PPUEntity activePPU = PPUServiceTestFixtures.createActivePPU();
//        List<BoardedEmployee> platformBoardingList = List.of(boardedEmployee);
//        List<SimpleEmployeeDTO> platformEmployeeDTOs = PPUServiceTestFixtures.createPlatformEmployees();
//        PPUResponse expectedPPUResponse = PPUResponse.copyWith(activePPU, getEmployeeUseCase.mapperDTO(currentUser),
//                "PlatformA");
//
//        when(userService.getEmployeeFromUser(userId)).thenReturn(currentUser);
//        when(etl.fetchBoardedEmployeesForDate(any())).thenReturn(List.of(boardedEmployee));
//        when(repository.findByPlatformsContainingAndStatus("PlatformA", DocumentStatus.ABERTO))
//                .thenReturn(Optional.of(activePPU));
//        when(etl.resolveSimpleEmployeesFromBoardedEmployees(platformBoardingList)).thenReturn(platformEmployeeDTOs);
//
//        PPUResponse result = ppuService.fetchCurrentPPU(userId, when);
//
//        assertNotNull(result);
//        assertEquals(expectedPPUResponse.getClass(), result.getClass());
//        verify(userService).getEmployeeFromUser(userId);
//        verify(etl).fetchBoardedEmployeesForDate(any());
//        verify(repository).findByPlatformsContainingAndStatus("PlatformA", DocumentStatus.ABERTO);
//        verify(etl).resolveSimpleEmployeesFromBoardedEmployees(platformBoardingList);
//    }
//
//    @Test
//    @DisplayName("Should throw NotFoundEmployee when fetching current PPU for non-boarded user")
//    void fetchCurrentPPU_ShouldThrowNotFoundEmployee_WhenUserNotBoarded() throws IOException {
//        UUID userId = UUID.randomUUID();
//        LocalDate when = LocalDate.now().minusDays(1);
//        EmployeeDTO currentUser = PPUServiceTestFixtures.createEmployeeDTO();
//
//        when(userService.getEmployeeFromUser(userId)).thenReturn(currentUser);
//        when(etl.fetchBoardedEmployeesForDate(any())).thenReturn(Collections.emptyList());
//
//        assertThrows(NotFoundEmployee.class, () -> ppuService.fetchCurrentPPU(userId, when));
//        verify(userService).getEmployeeFromUser(userId);
//        verify(etl).fetchBoardedEmployeesForDate(any());
//        verifyNoInteractions(repository, etl);
//    }
//
//    @Test
//    @DisplayName("Should throw IllegalArgumentException when fetching current PPU for boarded user without active PPU")
//    void fetchCurrentPPU_ShouldThrowIllegalArgumentException_WhenPPUNotFound() throws IOException {
//        UUID userId = UUID.randomUUID();
//        LocalDate when = LocalDate.now().minusDays(1);
//        EmployeeDTO currentUser = PPUServiceTestFixtures.createEmployeeDTO();
//        BoardedEmployee boardedEmployee = PPUServiceTestFixtures.createBoardedEmployee();
//
//        when(userService.getEmployeeFromUser(userId)).thenReturn(currentUser);
//        when(etl.fetchBoardedEmployeesForDate(any())).thenReturn(List.of(boardedEmployee));
//        when(repository.findByPlatformsContainingAndStatus("PlatformA", DocumentStatus.ABERTO))
//                .thenReturn(Optional.empty());
//
//        assertThrows(IllegalArgumentException.class, () -> ppuService.fetchCurrentPPU(userId, when));
//        verify(userService).getEmployeeFromUser(userId);
//        verify(etl).fetchBoardedEmployeesForDate(any());
//        verify(repository).findByPlatformsContainingAndStatus("PlatformA", DocumentStatus.ABERTO);
//        verifyNoMoreInteractions(etl);
//    }
//
//    @Test
//    @DisplayName("Should assign employees correctly when PPU has services and available employees")
//    void assignEmployeesToPPUServices_ShouldAssignEmployeesToServices() {
//        PPUEntity ppu = PPUServiceTestFixtures.createPPUWithServices();
//        List<SimpleEmployeeDTO> availableEmployees = PPUServiceTestFixtures.createAvailableEmployees();
//
//        List<SimpleEmployeeDTO> matchedEmployees1 = List.of(availableEmployees.get(0));
//        List<SimpleEmployeeDTO> matchedEmployees2 = List.of(availableEmployees.get(1));
//
//        when(etl.filterSimpleEmployeesByPosition(availableEmployees, List.of("pos1"))).thenReturn(matchedEmployees1);
//        when(etl.filterSimpleEmployeesByPosition(availableEmployees, List.of("pos2"))).thenReturn(matchedEmployees2);
//
//        ppuService.assignEmployeesToPPUServices(ppu, availableEmployees);
//
//        assertEquals(matchedEmployees1, ppu.getServices().get(0).getEmployees());
//        assertEquals(matchedEmployees2, ppu.getServices().get(1).getEmployees());
//        verify(etl).filterSimpleEmployeesByPosition(availableEmployees, List.of("pos1"));
//        verify(etl).filterSimpleEmployeesByPosition(availableEmployees, List.of("pos2"));
//    }
//
//    @Test
//    @DisplayName("Should do nothing when assigning employees to PPU without services")
//    void assignEmployeesToPPUServices_ShouldNotAssignEmployees_WhenServicesNull() {
//        PPUEntity ppu = new PPUEntity();
//        ppu.setServices(null);
//        List<SimpleEmployeeDTO> availableEmployees = PPUServiceTestFixtures.createAvailableEmployees();
//
//        ppuService.assignEmployeesToPPUServices(ppu, availableEmployees);
//
//        assertNull(ppu.getServices());
//        verifyNoInteractions(etl);
//    }
//
//    @Test
//    @DisplayName("Should update PPU when all fields are provided")
//    void updatePPU_ShouldUpdateAllFields_WhenAllFieldsProvided() {
//        String id = "1L";
//        PPURequest updateRecord = new PPURequest(
//                2L, // filialID
//                "Nova Filial", // filialName
//                2, // clienteID
//                2, // contratoID
//                Arrays.asList("Plataforma2"), // plataformas
//                Collections.emptyList(), // servicos
//                Collections.emptyList(), // equipamentos
//                Collections.emptyList(), // cabosAcos
//                Collections.emptyList(), // kitAcessorios
//                Collections.emptyList(), // controleGuindaste
//                Collections.emptyList(), // controleCaboAco
//                Arrays.asList("reg3", "reg4"), // disposicao
//                "Nova descrição", // descricao
//                LocalTime.of(7,0),
//                LocalTime.of(19,0),
//                LocalTime.of(19,0),
//                LocalTime.of(7,0)
//        );
//
//        PPUEntity existingPPU = new PPUEntity();
//        existingPPU.setId(id.toString());
//        ContractProject contract = new ContractProject();
//        List<SimpleEmployeeDTO> dispositionEmployees = Arrays.asList(
//                new SimpleEmployeeDTO("reg3", "Empregado 3", "cargo3", "Cargo 3", "sispat3"),
//                new SimpleEmployeeDTO("reg4", "Empregado 4", "cargo4", "Cargo 4", "sispat4"));
//
//        when(repository.findById(id)).thenReturn(Optional.of(existingPPU));
//        when(contractProjectRepository.findById(updateRecord.contratoID())).thenReturn(Optional.of(contract));
//        when(getEmployeeUseCase.findByRegistrations(updateRecord.disposicao())).thenReturn(dispositionEmployees);
//        when(repository.save(any(PPUEntity.class))).thenAnswer(i -> i.getArgument(0));
//
//        PPUEntity result = ppuService.updatePPU(id, updateRecord);
//
//        assertEquals(updateRecord.filialID(), result.getBranch());
//        assertEquals(updateRecord.filialName(), result.getBranchName());
//        assertEquals(contract, result.getContract());
//        assertEquals(updateRecord.plataformas(), result.getPlatforms());
//        assertEquals(dispositionEmployees, result.getEmployeeDisposition());
//        assertEquals(updateRecord.descricao(), result.getGeneralObservation());
//
//        verify(repository).findById(id);
//        verify(contractProjectRepository).findById(updateRecord.contratoID());
//        verify(getEmployeeUseCase).findByRegistrations(updateRecord.disposicao());
//        verify(repository).save(any(PPUEntity.class));
//    }
//
//    @Test
//    @DisplayName("Should update only provided fields when partial update")
//    void updatePPU_ShouldUpdateOnlyProvidedFields_WhenPartialUpdate() {
//        String id = "1L";
//        PPURequest updateRecord = new PPURequest(
//                null, // filialID
//                "Nova Filial", // filialName
//                null, // clienteID
//                null, // contratoID
//                null, // plataformas
//                null, // servicos
//                null, // equipamentos
//                null, // cabosAcos
//                null, // kitAcessorios
//                null, // controleGuindaste
//                null, // controleCaboAco
//                null, // disposicao
//                "Nova descrição", // descricao
//                null,
//                null,
//                null,
//                null
//        );
//
//        PPUEntity existingPPU = new PPUEntity();
//        existingPPU.setId(id.toString());
//        existingPPU.setBranch(1L);
//        existingPPU.setBranchName("Filial Original");
//        existingPPU.setGeneralObservation("Descrição original");
//
//        when(repository.findById(id)).thenReturn(Optional.of(existingPPU));
//        when(repository.save(any(PPUEntity.class))).thenAnswer(i -> i.getArgument(0));
//
//        PPUEntity result = ppuService.updatePPU(id, updateRecord);
//
//        assertEquals(1L, result.getBranch()); // Não deve ter mudado
//        assertEquals(updateRecord.filialName(), result.getBranchName());
//        assertEquals(updateRecord.descricao(), result.getGeneralObservation());
//
//        verify(repository).findById(id);
//        verify(repository).save(any(PPUEntity.class));
//        verifyNoInteractions(contractProjectRepository, getEmployeeUseCase);
//    }
//
//    @Test
//    @DisplayName("Should throw ModuleNotFoundFailure when PPU not found")
//    void updatePPU_ShouldThrowModuleNotFoundFailure_WhenPPUNotFound() {
//        String id = "1L";
//        PPURequest updateRecord = new PPURequest(
//                null, null, null, null, null, null, null, null, null, null, null, null, null,
//                null, null, null, null);
//
//        when(repository.findById(id)).thenReturn(Optional.empty());
//
//        assertThrows(ModuleNotFoundFailure.class, () -> ppuService.updatePPU(id, updateRecord));
//        verify(repository).findById(id);
//        verifyNoMoreInteractions(repository);
//        verifyNoInteractions(contractProjectRepository, getEmployeeUseCase);
//    }
//
//    @Test
//    @DisplayName("Should throw IllegalArgumentException when contract not found")
//    void updatePPU_ShouldThrowIllegalArgumentException_WhenContractNotFound() {
//        String id = "1L";
//        PPURequest updateRecord = new PPURequest(
//                null, null, null, 2, null, null, null, null, null, null, null, null, null,
//                null, null, null, null);
//
//        PPUEntity existingPPU = new PPUEntity();
//        existingPPU.setId(id.toString());
//
//        when(repository.findById(id)).thenReturn(Optional.of(existingPPU));
//        when(contractProjectRepository.findById(updateRecord.contratoID())).thenReturn(Optional.empty());
//
//        assertThrows(IllegalArgumentException.class, () -> ppuService.updatePPU(id, updateRecord));
//        verify(repository).findById(id);
//        verify(contractProjectRepository).findById(updateRecord.contratoID());
//        verifyNoMoreInteractions(repository);
//        verifyNoInteractions(getEmployeeUseCase);
//    }
//}