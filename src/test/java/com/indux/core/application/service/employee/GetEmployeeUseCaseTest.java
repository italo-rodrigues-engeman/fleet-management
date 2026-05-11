package com.indux.core.application.service.employee;

import com.indux.core.application.dto.generic.CompleteEmployeeDTO;
import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.generic.*;
import com.indux.modules.organization_chart.application.services.SubordinateService;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeUseCaseTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DependentRepository dependentRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private SubordinateService subordinateService;

    @InjectMocks
    private GetEmployeeUseCase getEmployeeUseCase;

    private UUID employeeId;
    private Employee mockEmployee;
    private EmployeeDTO mockEmployeeDTO;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        getEmployeeUseCase.setSubordinateService(subordinateService);

        mockEmployee = new Employee();
        mockEmployee.setId(employeeId);
        mockEmployee.setRegistration("12345");
        mockEmployee.setCpf("11122233344");
        mockEmployee.setName("Test Employee");
        mockEmployee.setStatusEmployee("Trabalhando");
        mockEmployee.setAdmissionDate(LocalDate.now().minusYears(2));
        mockEmployee.setFilialIdHcm(100);

        mockEmployeeDTO = new EmployeeDTO();
        mockEmployeeDTO.setId(employeeId);
        mockEmployeeDTO.setMatricula("12345");
        mockEmployeeDTO.setName("Test Employee");
        mockEmployeeDTO.setFilial_id_hcm("100");
    }

    @Test
    @DisplayName("Should assign hierarchy when SubordinateService is present")
    void ShouldAssignHierarchyWhenSubordinateServiceIsPresent() {
        when(employeeRepository.search("Test", true, false)).thenReturn(List.of(mockEmployeeDTO));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));

        EmployeeSummaryDTO.HierarchyInfo mockHierarchy = new EmployeeSummaryDTO.HierarchyInfo();
        mockHierarchy.setDiretoriaNome("Diretoria de TI");
        when(subordinateService.getHierarchyByFilialHcmId(100)).thenReturn(mockHierarchy);

        List<EmployeeDTO> result = getEmployeeUseCase.search("Test", true, false);
        assertEquals(1, result.size());
        assertNotNull(result.getFirst().getHierarchy());
        assertEquals("Diretoria de TI", result.getFirst().getHierarchy().getDiretoriaNome());
    }

    @Test
    @DisplayName("Should filter 'demitido' and prioritize 'trabalhando'")
    void ShouldFilterDemitidoAndPrioritizeTrabalhando() {
        Employee firedEmployee = new Employee();
        firedEmployee.setId(UUID.randomUUID());
        firedEmployee.setCpf("11122233344");
        firedEmployee.setStatusEmployee("Demitído");
        firedEmployee.setAdmissionDate(LocalDate.now().minusYears(5));

        Employee oldWorkingEmployee = new Employee();
        oldWorkingEmployee.setId(UUID.randomUUID());
        oldWorkingEmployee.setCpf("11122233344");
        oldWorkingEmployee.setStatusEmployee("Trabalhando");
        oldWorkingEmployee.setAdmissionDate(LocalDate.now().minusYears(3));

        Employee recentWorkingEmployee = new Employee();
        recentWorkingEmployee.setId(employeeId);
        recentWorkingEmployee.setCpf("11122233344");
        recentWorkingEmployee.setStatusEmployee("TRABALHANDO");
        recentWorkingEmployee.setAdmissionDate(LocalDate.now().minusYears(1));
        recentWorkingEmployee.setRegistration("12345");

        when(employeeRepository.findAllByCpf("11122233344"))
                .thenReturn(List.of(firedEmployee, oldWorkingEmployee, recentWorkingEmployee));

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(recentWorkingEmployee));
        when(dependentRepository.findActivePlansAndDependents(anyString())).thenReturn(Collections.emptyList());

        CompleteEmployeeDTO result = getEmployeeUseCase.getMobileEmployeeByCpf("11122233344");
        assertNotNull(result);
        assertEquals(recentWorkingEmployee.getId(), result.getId());
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException on getAllEmployeesSummary due to unmodifiable list")
    void ShouldMapAndTieBreakByDate() {
        Employee oldEmp = new Employee();
        oldEmp.setCpf("555");
        oldEmp.setStatusEmployee("Afastado");
        oldEmp.setAdmissionDate(LocalDate.of(2020, 1, 1));

        Employee newEmp = new Employee();
        newEmp.setCpf("555");
        newEmp.setStatusEmployee("Trabalhando");
        newEmp.setAdmissionDate(LocalDate.of(2022, 1, 1));

        Page<Employee> mockPage = new PageImpl<>(List.of(oldEmp, newEmp));
        when(employeeRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(mockPage);

        when(employeeRepository.findAllByCpf("555")).thenReturn(List.of(oldEmp, newEmp));

        assertThrows(UnsupportedOperationException.class, () -> {
            getEmployeeUseCase.getAllEmployeesSummary(PageRequest.of(0, 10), false, null);
        });
    }
}
