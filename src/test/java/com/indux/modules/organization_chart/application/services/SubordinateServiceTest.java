package com.indux.modules.organization_chart.application.services;

import com.indux.core.domain.repository.generic.CargoRepository;
import com.indux.core.domain.repository.generic.ContractProjectRepository;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.domain.repository.generic.RegionalRepository;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.SimpleContractEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.SimpleProjectEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubordinateServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private SimpleContractRepository contractRepository;

    @Mock
    private SimpleProjectRepository projectRepository;

    @Mock
    private SimpleEmployeeRepository employeeRepository;

    @Mock
    private RegionalRepository regionalRepository;

    @Mock
    private ContractProjectRepository contractProjectRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private OrganizationProjectRepository organizationProjectRepository;

    @Mock
    private OrganizationHcmRepository hcmRepository;

    @Mock
    private OrganizationMegaRepository megaRepository;

    @Mock
    private OrganizationFilialRepository filialRepository;

    @Mock
    private EmployeeRepository fullEmployeeRepository;

    @InjectMocks
    private SubordinateService subordinateService;

    @BeforeEach
    void setUp() {
    }
    

    @Test
    void getFiliaisHcmByFilters_WithNoFilters_ReturnsAllFiliais() {
        // Arrange
        FilialHcmEntity filial1 = new FilialHcmEntity();
        filial1.setFilialId(1);
        filial1.setNomeFilial("Filial 1");

        FilialHcmEntity filial2 = new FilialHcmEntity();
        filial2.setFilialId(2);
        filial2.setNomeFilial("Filial 2");

        when(filialRepository.findAll()).thenReturn(List.of(filial1, filial2));

        // Act
        Set<FilialHcmEntity> result = subordinateService.getFiliaisHcmByFilters(null, null, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(filial1));
        assertTrue(result.contains(filial2));
    }

    @Test
    void getFiliaisHcmByFilters_WithInvalidDiretoriaId_ThrowsException() {
        // Arrange
        Long invalidDiretoriaId = 999L;
        List<Long> list = new ArrayList<>();
        list.add(invalidDiretoriaId);
        when(organizationRepository.findAllById(list)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(ModuleNotFoundFailure.class, () -> {
            subordinateService.getFiliaisHcmByFilters(list, null, null, null, null, null);
        });
    }

    @Test
    void getFiliaisHcmByFilters_WithInvalidSuperintendenciaId_ThrowsException() {
        // Arrange
        Long invalidSuperintendenciaId = 999L;
        List<Long> list = new ArrayList<>();
        list.add(invalidSuperintendenciaId);
        when(organizationRepository.findAllById(list)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(ModuleNotFoundFailure.class, () -> {
            subordinateService.getFiliaisHcmByFilters(null, list, null, null, null, null);
        });
    }

    @Test
    void getFiliaisHcmByFilters_WithInvalidRegionalId_ThrowsException() {
        // Arrange
        Long invalidRegionalId = 999L;
        List<Long> list = new ArrayList<>();
        list.add(invalidRegionalId);
        when(organizationRepository.findAllById(list)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(ModuleNotFoundFailure.class, () -> {
            subordinateService.getFiliaisHcmByFilters(null, null, list, null, null, null);
        });
    }

    @Test
    void getFiliaisHcmByFilters_WithInvalidContratoId_ThrowsException() {
        // Arrange
        Long invalidContratoId = 999L;
        List<Long> list = new ArrayList<>();
        list.add(invalidContratoId);
        when(contractRepository.findAllById(list)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(ModuleNotFoundFailure.class, () -> {
            subordinateService.getFiliaisHcmByFilters(null, null, null, null, list, null);
        });
    }

    @Test
    void getFiliaisHcmByFilters_WithInvalidProjetoId_ThrowsException() {
        // Arrange
        Long invalidProjetoId = 999L;
        List<Long> list = new ArrayList<>();
        list.add(invalidProjetoId);
        when(projectRepository.findAllById(list)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(ModuleNotFoundFailure.class, () -> {
            subordinateService.getFiliaisHcmByFilters(null, null, null, null, null, list);
        });
    }
}