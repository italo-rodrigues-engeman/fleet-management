package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.NiOrganizationCreate;
import com.indux.modules.calibration.aplication.dtos.NiOrganizationReturn;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import com.indux.modules.calibration.domain.entities.mongo.NiOrganizationEntity;
import com.indux.modules.calibration.domain.entities.mongo.RangeEntity;
import com.indux.modules.calibration.domain.entities.mongo.ToleranceEntity;
import com.indux.modules.calibration.domain.repository.mongo.CalibrationStandardRepository;
import com.indux.modules.calibration.domain.repository.mongo.NiOrganizationRepository;
import com.indux.modules.calibration.infra.mappers.CalibrationMapper;
import com.indux.modules.calibration.infra.mappers.CalibrationStandardMapper;
import com.indux.modules.calibration.infra.mappers.GeralMapper;
import com.indux.modules.cdi.aplication.service.CounterService;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.domain.entities.jpa.ContractEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationContractRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationProjectRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NiOrganizationServiceTest {

    @Mock
    private NiOrganizationRepository niOrganizationRepository;

    @Mock
    private GeralMapper geralMapper;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationContractRepository contractRepository;

    @Mock
    private OrganizationProjectRepository projectRepository;

    @Mock
    private CalibrationStandardRepository calibrationStandardRepository;

    @Mock
    private CalibrationMapper calibrationMapper;
    
    @Mock
    private CalibrationStandardMapper calibrationStandardMapper;

    @Mock
    private CounterService counterService;

    @InjectMocks
    private NiOrganizationService niOrganizationService;

    private NiOrganizationCreate niOrganizationCreateDto;
    private NiOrganizationEntity niOrganizationEntity;
    private CalibrationStandardEntity calibrationStandardEntity;

    @BeforeEach
    void setUp() {
        List<Long> branchIds = new ArrayList<>();
        branchIds.add(1L);
        List<Long> contractIds = new ArrayList<>();
        contractIds.add(2L);
        List<Long> projectIds = new ArrayList<>();
        projectIds.add(3L);
        List<String> propertiesIds = new ArrayList<>();
        propertiesIds.add("property-id");

        niOrganizationCreateDto = new NiOrganizationCreate(
            "NI-MEGA-001",
            "standard-id",
            branchIds,
            contractIds,
            projectIds,
            propertiesIds,
            "Observação de teste"
        );

        niOrganizationEntity = new NiOrganizationEntity();
        niOrganizationEntity.setId("ni-org-id");
        niOrganizationEntity.setHeritage("NI-MEGA-001");
        niOrganizationEntity.setBranchIds(branchIds);
        niOrganizationEntity.setContractIds(contractIds);
        niOrganizationEntity.setProjectIds(projectIds);
        niOrganizationEntity.setObservation("Observação de teste");

        calibrationStandardEntity = new CalibrationStandardEntity();
        calibrationStandardEntity.setId("standard-id");
    }

    @Test
    @DisplayName("Should Create NiOrganization Successfully")
    void shouldCreateNiOrganizationSuccessfully() {
        when(counterService.getNextSequence(anyString())).thenReturn(1L);
        when(geralMapper.niOrganizationEntity(any(NiOrganizationCreate.class))).thenReturn(niOrganizationEntity);
        when(calibrationStandardRepository.findById("standard-id")).thenReturn(Optional.of(calibrationStandardEntity));

        niOrganizationService.createNiOrganization(niOrganizationCreateDto);

        verify(counterService).getNextSequence("calibration_organization_sequence");
        verify(geralMapper).niOrganizationEntity(niOrganizationCreateDto);
        verify(calibrationStandardRepository).findById("standard-id");
        verify(niOrganizationRepository).save(niOrganizationEntity);
    }

    @Test
    @DisplayName("Shold Throw Exception When Standard Not Found During Creation")
    void shouldThrowExceptionWhenStandardNotFoundDuringCreation() {
        when(counterService.getNextSequence(anyString())).thenReturn(1L);
        when(geralMapper.niOrganizationEntity(any(NiOrganizationCreate.class))).thenReturn(niOrganizationEntity);
        when(calibrationStandardRepository.findById("standard-id")).thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () -> niOrganizationService.createNiOrganization(niOrganizationCreateDto));
        verify(counterService).getNextSequence("calibration_organization_sequence");
        verify(geralMapper).niOrganizationEntity(niOrganizationCreateDto);
        verify(calibrationStandardRepository).findById("standard-id");
        verify(niOrganizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should Get All NiOrganizations")
    void shouldGetAllNiOrganizations() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<NiOrganizationEntity> entityPage = new PageImpl<>(List.of(niOrganizationEntity), pageable, 1);
        
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(1L);
        organizationEntity.setAcronym("ORG");
        
        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setId(2L);
        
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(3L);
        
        when(niOrganizationRepository.findAll(pageable)).thenReturn(entityPage);
        when(organizationRepository.findAllById(anyList())).thenReturn(List.of(organizationEntity));
        when(contractRepository.findAllById(anyList())).thenReturn(List.of(contractEntity));
        when(projectRepository.findAllById(anyList())).thenReturn(List.of(projectEntity));

        Page<NiOrganizationReturn> result = niOrganizationService.getAllNiOrganizations(pageable, null, null);

        assertThat(result).isNotNull();
        verify(niOrganizationRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should Update NiOrganization Successfully")
    void shouldUpdateNiOrganizationSuccessfully() {
        String id = "ni-org-id";
        when(niOrganizationRepository.findById(id)).thenReturn(Optional.of(niOrganizationEntity));
        when(geralMapper.niOrganizationEntity(any(NiOrganizationCreate.class))).thenReturn(niOrganizationEntity);
        when(calibrationStandardRepository.findById("standard-id")).thenReturn(Optional.of(calibrationStandardEntity));

        niOrganizationService.updateNiOrganization(id, niOrganizationCreateDto);

        verify(niOrganizationRepository).findById(id);
        verify(geralMapper).niOrganizationEntity(niOrganizationCreateDto);
        verify(calibrationStandardRepository).findById("standard-id");
        verify(niOrganizationRepository).save(niOrganizationEntity);
    }

    @Test
    @DisplayName("Should Throw Exception When Updating Non Existent NiOrganization")
    void shouldThrowExceptionWhenUpdatingNonExistentNiOrganization() {
        String id = "non-existent-id";
        when(niOrganizationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () -> niOrganizationService.updateNiOrganization(id, niOrganizationCreateDto));
        verify(niOrganizationRepository).findById(id);
        verify(niOrganizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should Set Tolerance Organograma With Separate Lists For Tolerances")
    void shouldSetToleranceOrganogramaWithSeparateListsForTolerances() {
        String id = "ni-org-id";
        
        NiOrganizationEntity heritage = new NiOrganizationEntity();
        heritage.setId(id);
        heritage.setHeritage("NI-MEGA-001");
        
        CalibrationStandardEntity standard = new CalibrationStandardEntity();
        standard.setId("standard-id");
        
        RangeEntity range = new RangeEntity();
        range.setId("range-id");
        
        ToleranceEntity tolerance1 = new ToleranceEntity();
        tolerance1.setId("tolerance1-id");
        tolerance1.setBranchId(Arrays.asList(1L, 2L));
        tolerance1.setContractId(Arrays.asList(10L));
        tolerance1.setProjectId(Arrays.asList(100L));
        
        ToleranceEntity tolerance2 = new ToleranceEntity();
        tolerance2.setId("tolerance2-id");
        tolerance2.setBranchId(Arrays.asList(3L));
        tolerance2.setContractId(Arrays.asList(20L, 30L));
        tolerance2.setProjectId(Arrays.asList(200L, 300L));
        
        range.setTolerance(Arrays.asList(tolerance1, tolerance2));
        standard.setRange(Arrays.asList(range));
        heritage.setCalibrationStandard(standard);
        
        when(niOrganizationRepository.findById(id)).thenReturn(Optional.of(heritage));
        
        OrganizationEntity org1 = new OrganizationEntity();
        org1.setId(1L);
        org1.setAcronym("ORG1");
        
        OrganizationEntity org2 = new OrganizationEntity();
        org2.setId(2L);
        org2.setAcronym("ORG2");
        
        OrganizationEntity org3 = new OrganizationEntity();
        org3.setId(3L);
        org3.setAcronym("ORG3");
        
        when(organizationRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(org1, org2));
        when(organizationRepository.findAllById(Arrays.asList(3L))).thenReturn(Arrays.asList(org3));
        
        ContractEntity contract1 = new ContractEntity();
        contract1.setId(10L);
        
        when(contractRepository.findAllById(Arrays.asList(10L))).thenReturn(Arrays.asList(contract1));
        
        ContractEntity contract2 = new ContractEntity();
        contract2.setId(20L);
        
        ContractEntity contract3 = new ContractEntity();
        contract3.setId(30L);
        
        when(contractRepository.findAllById(Arrays.asList(20L, 30L))).thenReturn(Arrays.asList(contract2, contract3));
        
        ProjectEntity project1 = new ProjectEntity();
        project1.setId(100L);
        
        when(projectRepository.findAllById(Arrays.asList(100L))).thenReturn(Arrays.asList(project1));
        
        ProjectEntity project2 = new ProjectEntity();
        project2.setId(200L);
        
        ProjectEntity project3 = new ProjectEntity();
        project3.setId(300L);
        
        when(projectRepository.findAllById(Arrays.asList(200L, 300L))).thenReturn(Arrays.asList(project2, project3));
        
        OrganizationDTO orgDto1 = new OrganizationDTO(1L, "ORG1", null, null, null, null, null, null, null, null);
        OrganizationDTO orgDto2 = new OrganizationDTO(2L, "ORG2", null, null, null, null, null, null, null, null);
        OrganizationDTO orgDto3 = new OrganizationDTO(3L, "ORG3", null, null, null, null, null, null, null, null);
        
        when(calibrationMapper.toOrganizationDTO(org1)).thenReturn(orgDto1);
        when(calibrationMapper.toOrganizationDTO(org2)).thenReturn(orgDto2);
        when(calibrationMapper.toOrganizationDTO(org3)).thenReturn(orgDto3);
        
        ContractDTO contractDto1 = new ContractDTO(10L, null, null, null, null, null, null, null);
        ContractDTO contractDto2 = new ContractDTO(20L, null, null, null, null, null, null, null);
        ContractDTO contractDto3 = new ContractDTO(30L, null, null, null, null, null, null, null);
        
        when(calibrationMapper.toContractDTO(contract1)).thenReturn(contractDto1);
        when(calibrationMapper.toContractDTO(contract2)).thenReturn(contractDto2);
        when(calibrationMapper.toContractDTO(contract3)).thenReturn(contractDto3);

        niOrganizationService.getHeritageById(id);

        assertEquals(2, tolerance1.getBranch().size());
        assertTrue(tolerance1.getBranch().stream().anyMatch(org -> org.id() == 1L));
        assertTrue(tolerance1.getBranch().stream().anyMatch(org -> org.id() == 2L));
        
        assertEquals(1, tolerance1.getContract().size());
        assertEquals(10L, tolerance1.getContract().get(0).id());
        
        assertEquals(1, tolerance1.getProject().size());
        assertEquals(100L, tolerance1.getProject().get(0).id());
        
        assertEquals(1, tolerance2.getBranch().size());
        assertEquals(3L, tolerance2.getBranch().get(0).id());
        
        assertEquals(2, tolerance2.getContract().size());
        assertTrue(tolerance2.getContract().stream().anyMatch(contract -> contract.id() == 20L));
        assertTrue(tolerance2.getContract().stream().anyMatch(contract -> contract.id() == 30L));
        
        assertEquals(2, tolerance2.getProject().size());
        assertTrue(tolerance2.getProject().stream().anyMatch(project -> project.id() == 200L));
        assertTrue(tolerance2.getProject().stream().anyMatch(project -> project.id() == 300L));
    }
}