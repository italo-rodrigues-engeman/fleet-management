package com.indux.core.application.service.cbo;

import com.indux.core.application.dto.cbo.CBODTO;
import com.indux.core.application.dto.cbo.FilterCBO;
import com.indux.core.application.dto.cbo.GetAllCBO;
import com.indux.core.application.mapper.CBOMapper;
import com.indux.core.domain.model.cbo.CBODetails;
import com.indux.core.domain.model.cbo.FuncaoHCM;
import com.indux.core.application.dto.cbo.RelatedPosition;
import com.indux.core.domain.model.employee.Cargo;
import com.indux.core.domain.repository.cbo.CBODetailsRepository;
import com.indux.core.domain.repository.cbo.FuncaoHCMRepository;
import com.indux.core.domain.repository.generic.CargoRepository;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.organization_chart.application.services.FilialService;
import com.indux.modules.organization_chart.application.services.SubordinateService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.training.application.service.TrainingService;
import com.indux.modules.training.infra.mappers.TrainingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CBODetailsServiceTest {

    @Mock
    private CBODetailsRepository cboDetailsRepository;
    @Mock
    private CargoRepository cargoRepository;
    @Mock
    private CBOMapper cboMapper;
    @Mock
    private TrainingService trainingService;
    @Mock
    private FuncaoHCMRepository funcaoHCMRepository;
    @Mock
    private TrainingMapper trainingMapper;
    @Mock
    private SubordinateService subordinateService;
    @Mock
    private FilialService filialService;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CBODetailsService cboDetailsService;

    private Cargo mockCargo;
    private Pageable mockPageable;
    private FilialHcmEntity mockFilial;

    @BeforeEach
    void setUp() {
        mockPageable = PageRequest.of(0, 10);
        mockFilial = new FilialHcmEntity();
        mockFilial.setFilialId(10);

        mockCargo = new Cargo();
        mockCargo.setId(1L);
        mockCargo.setCodeCbo("1234");
        mockCargo.setNameTitleCbo("Desenvolvedor");
        mockCargo.setIdHcm("HCM-999");
        mockCargo.setFilial(new ArrayList<>(List.of(mockFilial)));
    }

    @Test
    @DisplayName("Should populate filter with subordinate filial array when filter has regional data")
    void ShouldPopulateFilterWithSubordinateFilialArrayWhenFilterHasRegionalData() {
        FilterCBO filter = new FilterCBO();
        filter.setSetorId(List.of(1L));

        FilialHcmEntity filialResult = new FilialHcmEntity();
        filialResult.setFilialId(50);
        when(subordinateService.getFiliaisHcmByFilters(null, null, null, List.of(1L), null, null))
                .thenReturn(Set.of(filialResult));

        when(cargoRepository.findFilterCargo(any(FilterCBO.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        cboDetailsService.getAll(filter, mockPageable);

        assertNotNull(filter.getFilialIdHcm());
        assertTrue(filter.getFilialIdHcm().contains(50));
        verify(subordinateService, times(1)).getFiliaisHcmByFilters(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should bypass subordinate service when filter lacks regional data")
    void ShouldBypassSubordinateServiceWhenFilterLacksRegionalData() {
        FilterCBO filter = new FilterCBO();

        when(cargoRepository.findFilterCargo(any(FilterCBO.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        cboDetailsService.getAll(filter, mockPageable);

        verify(subordinateService, never()).getFiliaisHcmByFilters(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should correctly map Cargo to GetAllCBO extracting organization unique fields")
    void ShouldCorrectlyMapCargoToGetAllCboExtractingOrganizationUniqueFields() {
        FilterCBO filter = new FilterCBO();

        when(cargoRepository.findFilterCargo(any(), any())).thenReturn(new PageImpl<>(List.of(mockCargo)));

        Map<String, Object> orgMap = new HashMap<>();
        orgMap.put("nomeRegional", "Sul");
        orgMap.put("nomeContrato", "XPTO");

        Map<String, Object> orgMapDup = new HashMap<>();
        orgMapDup.put("nomeRegional", "Sul");
        orgMapDup.put("nomeContrato", "ABC");

        when(filialService.filialOrganization(List.of(10))).thenReturn(List.of(orgMap, orgMapDup));

        when(employeeRepository.countActiveEmployeesByCargoAndFiliais("HCM-999", List.of(10))).thenReturn(5L);
        when(employeeRepository.countAllEmployeesByCargoAndFiliais("HCM-999", List.of(10))).thenReturn(8L);

        when(funcaoHCMRepository.findByHcmId("HCM-999")).thenReturn(Optional.of(new FuncaoHCM()));

        Page<GetAllCBO> result = cboDetailsService.getAll(filter, mockPageable);

        assertFalse(result.isEmpty());
        GetAllCBO mapped = result.getContent().getFirst();
        assertEquals("1234", mapped.codCBO());
        assertEquals("HCM-999", mapped.idHCM());
        assertEquals(5L, mapped.activo());
        assertEquals(8L, mapped.totalPerson());
        assertEquals(1, mapped.branch().size());
        assertEquals("Sul", mapped.branch().getFirst());
        assertEquals(2, mapped.contract().size());
    }

    @Test
    @DisplayName("Should catch repository exceptions on getTrainingsForCargo returning abstract empty list")
    void ShouldCatchRepositoryExceptionsOnGetTrainingsForCargoReturningAbstractEmptyList() {
        FilterCBO filter = new FilterCBO();
        when(cargoRepository.findFilterCargo(any(), any())).thenReturn(new PageImpl<>(List.of(mockCargo)));
        when(filialService.filialOrganization(anyList())).thenReturn(Collections.emptyList());

        when(funcaoHCMRepository.findByHcmId("HCM-999")).thenThrow(new RuntimeException("DB Failure"));

        assertDoesNotThrow(() -> {
            Page<GetAllCBO> result = cboDetailsService.getAll(filter, mockPageable);
            assertTrue(result.getContent().getFirst().trainings().isEmpty());
        });
    }

    @Test
    @DisplayName("Should extract related position from multiple mappings respecting manual pagination limits")
    void ShouldExtractRelatedPositionFromMultipleMappingsRespectingManualPaginationLimits() {
        CBODetails cbo1 = new CBODetails();
        cbo1.setRelatedPosition(List.of(
            new RelatedPosition(1, null, null),
            new RelatedPosition(2, null, null)
        ));

        CBODetails cbo2 = new CBODetails();
        cbo2.setRelatedPosition(List.of(
            new RelatedPosition(3, null, null)
        ));

        when(cboDetailsRepository.findAll()).thenReturn(List.of(cbo1, cbo2));

        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<RelatedPosition> result = cboDetailsService.findAllCBO(pageRequest);

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().getFirst().codCBO());
    }

    @Test
    @DisplayName("Should map manual pagination to empty list when start bounds are greater than total elements")
    void ShouldMapManualPaginationToEmptyListWhenStartBoundsAreGreaterThanTotalElements() {
        CBODetails cbo1 = new CBODetails();
        cbo1.setRelatedPosition(List.of(new RelatedPosition(1, null, null)));

        when(cboDetailsRepository.findAll()).thenReturn(List.of(cbo1));

        PageRequest pageRequestExtremelyHigh = PageRequest.of(5, 10);
        Page<RelatedPosition> result = cboDetailsService.findAllCBO(pageRequestExtremelyHigh);

        assertTrue(result.getContent().isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should return related CBO DTO when code matches nested item")
    void ShouldReturnRelatedCboDtoWhenCodeMatchesNestedItem() {
        CBODetails cbo1 = new CBODetails();
        cbo1.setId("ABC");
        cbo1.setRelatedPosition(List.of(new RelatedPosition(999, null, null)));

        when(cboDetailsRepository.findAll()).thenReturn(List.of(cbo1));

        CBODTO mockDto = new CBODTO("ABC", null, null, null, null, null, null, null, null, null, null);
        when(cboMapper.toDTO(cbo1)).thenReturn(mockDto);

        CBODTO result = cboDetailsService.findByIdCBO(999);
        assertEquals("ABC", result.id());
    }

    @Test
    @DisplayName("Should throw ModuleNotFoundException when CBO code is not matched via nested structures")
    void ShouldThrowModuleNotFoundExceptionWhenCboCodeIsNotMatchedViaNestedStructures() {
        CBODetails cbo1 = new CBODetails();
        cbo1.setRelatedPosition(List.of(new RelatedPosition(555, null, null)));

        when(cboDetailsRepository.findAll()).thenReturn(List.of(cbo1));

        assertThrows(ModuleNotFoundFailure.class, () -> cboDetailsService.findByIdCBO(999));
    }

    @Test
    @DisplayName("Should initialize new filial list and add to cargo if cargo filial list is null in postMatriz")
    void ShouldInitializeNewFilialListAndAddToCargoIfCargoFilialListIsNullInPostMatriz() {
        when(filialService.getFilialById(10)).thenReturn(Optional.of(mockFilial));
        Cargo cargoNoFilial = new Cargo();
        cargoNoFilial.setId(1L);

        when(cargoRepository.findById("CX1")).thenReturn(Optional.of(cargoNoFilial));

        cboDetailsService.postMatriz(10, List.of("CX1"));

        assertNotNull(cargoNoFilial.getFilial());
        assertEquals(1, cargoNoFilial.getFilial().size());
        assertEquals(10, cargoNoFilial.getFilial().getFirst().getFilialId());
        verify(cargoRepository, times(1)).save(cargoNoFilial);
    }

    @Test
    @DisplayName("Should ignore postMatriz operation entirely when cargo already holds the specific filial mapped")
    void ShouldIgnorePostMatrizOperationEntirelyWhenCargoAlreadyHoldsTheSpecificFilialMapped() {
        when(filialService.getFilialById(10)).thenReturn(Optional.of(mockFilial));
        
        Cargo cargoWithFilial = new Cargo();
        cargoWithFilial.setId(1L);
        cargoWithFilial.setFilial(new ArrayList<>(List.of(mockFilial)));

        when(cargoRepository.findById("CX1")).thenReturn(Optional.of(cargoWithFilial));

        cboDetailsService.postMatriz(10, List.of("CX1"));

        verify(cargoRepository, never()).save(cargoWithFilial);
    }

    @Test
    @DisplayName("Should successfully remove link on deleteMatriz causing branch deletion from model")
    void ShouldSuccessfullyRemoveLinkOnDeleteMatrizCausingBranchDeletionFromModel() {
        when(filialService.getFilialById(10)).thenReturn(Optional.of(mockFilial));
        
        Cargo cargoWithFilial = new Cargo();
        cargoWithFilial.setId(1L);
        cargoWithFilial.setFilial(new ArrayList<>(List.of(mockFilial)));

        when(cargoRepository.findById("CX1")).thenReturn(Optional.of(cargoWithFilial));

        cboDetailsService.deleteMatriz(10, List.of("CX1"));

        assertTrue(cargoWithFilial.getFilial().isEmpty());
        verify(cargoRepository, times(1)).save(cargoWithFilial);
    }

    @Test
    @DisplayName("Should abort deleteMatriz sequence and not raise exceptions if link misses")
    void ShouldAbortDeleteMatrizSequenceAndNotRaiseExceptionsIfLinkMisses() {
        when(filialService.getFilialById(10)).thenReturn(Optional.of(mockFilial));
        
        Cargo cargoEmpty = new Cargo();
        cargoEmpty.setId(1L);
        cargoEmpty.setFilial(new ArrayList<>());

        when(cargoRepository.findById("CX1")).thenReturn(Optional.of(cargoEmpty));

        cboDetailsService.deleteMatriz(10, List.of("CX1"));

        verify(cargoRepository, never()).save(cargoEmpty);
    }

    @Test
    @DisplayName("Should throw if Filial misses in postMatriz or deleteMatriz loops")
    void ShouldThrowIfFilialMissesInPostMatrizOrDeleteMatrizLoops() {
        when(filialService.getFilialById(99)).thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundFailure.class, () -> cboDetailsService.postMatriz(99, List.of("1")));
        assertThrows(ModuleNotFoundFailure.class, () -> cboDetailsService.deleteMatriz(99, List.of("1")));
    }
}
