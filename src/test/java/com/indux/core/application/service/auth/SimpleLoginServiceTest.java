package com.indux.core.application.service.auth;

import com.indux.core.application.dto.auth.SimpleLoginRequestDTO;
import com.indux.core.application.dto.auth.SimpleLoginResponseDTO;
import com.indux.core.application.dto.generic.CompleteEmployeeDTO;
import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.model.employee.Cargo;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.employee.Regional;
import com.indux.core.domain.repository.generic.CargoRepository;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.domain.repository.generic.RegionalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleLoginServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private GetEmployeeUseCase getEmployeeUseCase;

    @Mock
    private RegionalRepository regionalRepository;

    @InjectMocks
    private SimpleLoginService simpleLoginService;

    @Test
    @DisplayName("should authenticate employee successfully")
    void testAuthenticate_Success() {
        UUID employeeId = UUID.randomUUID();
        SimpleLoginRequestDTO request = new SimpleLoginRequestDTO();
        request.setCpf("12345678901");
        request.setNomeCompleto("João Silva");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));

        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setCpf("12345678901");
        employee.setName("João Silva");
        employee.setBirthDate(LocalDate.of(1990, 1, 1));
        employee.setAdmissionDate(LocalDate.of(2020, 1, 1));
        employee.setPosition("CARGO_1");
        employee.setBranch_id(2L);

        when(employeeRepository.findAllByCpf("12345678901")).thenReturn(List.of(employee));

        CompleteEmployeeDTO employeeInfo = new CompleteEmployeeDTO();
        employeeInfo.setNomeProjeto("Projeto A");
        employeeInfo.setCentro_custos_id("COST_1");

        EmployeeSummaryDTO.HierarchyInfo hierarchy = new EmployeeSummaryDTO.HierarchyInfo();
        hierarchy.setRegionalNome("Região Sul");
        hierarchy.setContratoNome("Contrato Fixo");
        employeeInfo.setHierarchy(hierarchy);

        when(getEmployeeUseCase.getEmployeeById(employeeId)).thenReturn(employeeInfo);

        Cargo cargo = new Cargo();
        cargo.setIdHcm("CARGO_1");
        cargo.setNameTitle("Desenvolvedor");
        when(cargoRepository.findByIdHcm("CARGO_1")).thenReturn(Optional.of(cargo));

        Regional regional = new Regional();
        regional.setRegional("REG_SUL");
        when(regionalRepository.findByFilialId(2L)).thenReturn(Optional.of(regional));

        SimpleLoginResponseDTO response = simpleLoginService.authenticate(request);

        assertTrue(response.isSuccess());
        assertEquals("João Silva", response.getNome());
        assertEquals("Desenvolvedor", response.getCargoNome());
        assertEquals("Região Sul", response.getRegionalOG());
        assertEquals("Contrato Fixo", response.getContratoOG());
        verify(employeeRepository).findAllByCpf(any());
        verify(getEmployeeUseCase).getEmployeeById(employeeId);
    }

    @Test
    @DisplayName("should select the most recent employee when there is duplication")
    void testAuthenticateMultipleEmployeesChooseMostRecent() {
        UUID oldId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();
        SimpleLoginRequestDTO request = new SimpleLoginRequestDTO();
        request.setCpf("12345678901");
        request.setNomeCompleto("João Silva");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));

        Employee oldEmployee = new Employee();
        oldEmployee.setId(oldId);
        oldEmployee.setName("João Silva");
        oldEmployee.setBirthDate(LocalDate.of(1990, 1, 1));
        oldEmployee.setAdmissionDate(LocalDate.of(2015, 1, 1));

        Employee newEmployee = new Employee();
        newEmployee.setId(newId);
        newEmployee.setName("João Silva");
        newEmployee.setBirthDate(LocalDate.of(1990, 1, 1));
        newEmployee.setAdmissionDate(LocalDate.of(2022, 1, 1));

        when(employeeRepository.findAllByCpf("12345678901")).thenReturn(List.of(oldEmployee, newEmployee));
        when(getEmployeeUseCase.getEmployeeById(newId)).thenReturn(new CompleteEmployeeDTO());

        SimpleLoginResponseDTO response = simpleLoginService.authenticate(request);

        assertTrue(response.isSuccess());
        assertEquals(newId, response.getFuncionarioId());
    }

    @Test
    @DisplayName("should ignore admission date 1900-12-31 in the filter, using fallback if it is the only one")
    void testAuthenticateFallbackEmptyValidAdmissions() {
        UUID validId = UUID.randomUUID();
        SimpleLoginRequestDTO request = new SimpleLoginRequestDTO();
        request.setCpf("12345678901");
        request.setNomeCompleto("João Silva");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));

        Employee invalidDateEmployee = new Employee();
        invalidDateEmployee.setId(validId);
        invalidDateEmployee.setName("João Silva");
        invalidDateEmployee.setBirthDate(LocalDate.of(1990, 1, 1));
        invalidDateEmployee.setAdmissionDate(LocalDate.of(1900, 12, 31));

        when(employeeRepository.findAllByCpf("12345678901")).thenReturn(List.of(invalidDateEmployee));
        when(getEmployeeUseCase.getEmployeeById(validId)).thenReturn(new CompleteEmployeeDTO());

        SimpleLoginResponseDTO response = simpleLoginService.authenticate(request);

        assertTrue(response.isSuccess());
        assertEquals(validId, response.getFuncionarioId());
    }

    @Test
    @DisplayName("should register 'Not informed' if DTO Hierarchy is null")
    void testAuthenticateHierarchyNullOrMissing() {
        UUID missingId = UUID.randomUUID();
        SimpleLoginRequestDTO request = new SimpleLoginRequestDTO();
        request.setCpf("010101");
        request.setNomeCompleto("User");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));

        Employee emp = new Employee();
        emp.setId(missingId);
        emp.setName("User");
        emp.setBirthDate(LocalDate.of(1990, 1, 1));

        when(employeeRepository.findAllByCpf("010101")).thenReturn(List.of(emp));

        CompleteEmployeeDTO dtoWithoutHierarchy = new CompleteEmployeeDTO();
        dtoWithoutHierarchy.setHierarchy(null);
        when(getEmployeeUseCase.getEmployeeById(missingId)).thenReturn(dtoWithoutHierarchy);

        SimpleLoginResponseDTO response = simpleLoginService.authenticate(request);

        assertTrue(response.isSuccess());
        assertEquals("Não informado", response.getRegionalOG());
        assertEquals("Não informado", response.getContratoOG());
    }

    @Test
    @DisplayName("should succeed even if there is an error in the search for job/regional info")
    void testAuthenticateTolerateErrorsOnSecondaryQueries() {
        UUID employeeId = UUID.randomUUID();
        SimpleLoginRequestDTO request = new SimpleLoginRequestDTO();
        request.setCpf("010101");
        request.setNomeCompleto("User");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));

        Employee emp = new Employee();
        emp.setId(employeeId);
        emp.setName("User");
        emp.setPosition("POS");
        emp.setBranch_id(100L);
        emp.setBirthDate(LocalDate.of(1990, 1, 1));

        when(employeeRepository.findAllByCpf("010101")).thenReturn(List.of(emp));
        when(getEmployeeUseCase.getEmployeeById(employeeId)).thenReturn(new CompleteEmployeeDTO());

        when(cargoRepository.findByIdHcm(anyString())).thenThrow(new RuntimeException("DB Outage"));

        SimpleLoginResponseDTO response = simpleLoginService.authenticate(request);

        assertTrue(response.isSuccess());
        assertNull(response.getCargoNome());
    }

    @Test
    @DisplayName("should normalize the name ignoring spaces and case mismatch")
    void testAuthenticateCaseAndSpaceInsensitive() {
        UUID employeeId = UUID.randomUUID();
        SimpleLoginRequestDTO request = new SimpleLoginRequestDTO();
        request.setCpf("123");
        request.setNomeCompleto(" joao silva  ");
        request.setDataNascimento(LocalDate.of(1980, 1, 1));

        Employee emp = new Employee();
        emp.setId(employeeId);
        emp.setName("JOAO SILVA");
        emp.setBirthDate(LocalDate.of(1980, 1, 1));

        when(employeeRepository.findAllByCpf("123")).thenReturn(List.of(emp));
        when(getEmployeeUseCase.getEmployeeById(employeeId)).thenReturn(new CompleteEmployeeDTO());

        SimpleLoginResponseDTO response = simpleLoginService.authenticate(request);

        assertTrue(response.isSuccess());
    }

    @Test
    @DisplayName("should fail if no employee is found with the CPF")
    void testAuthenticateFailEmployeeNotFound() {
        SimpleLoginRequestDTO request = new SimpleLoginRequestDTO();
        request.setCpf("000");

        when(employeeRepository.findAllByCpf("000")).thenReturn(Collections.emptyList());

        SimpleLoginResponseDTO response = simpleLoginService.authenticate(request);

        assertFalse(response.isSuccess());
        assertEquals("Funcionário não encontrado", response.getMessage());
    }

    @Test
    @DisplayName("should fail if the name is divergent")
    void testAuthenticateFailNameMismatch() {
        SimpleLoginRequestDTO request = new SimpleLoginRequestDTO();
        request.setCpf("123");
        request.setNomeCompleto("Maria Silva");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));

        Employee emp = new Employee();
        emp.setName("João Silva");
        emp.setBirthDate(LocalDate.of(1990, 1, 1));

        when(employeeRepository.findAllByCpf("123")).thenReturn(List.of(emp));

        SimpleLoginResponseDTO response = simpleLoginService.authenticate(request);

        assertFalse(response.isSuccess());
        assertEquals("Dados de acesso incorretos", response.getMessage());
    }

    @Test
    @DisplayName("should fail if the birth date is divergent")
    void testAuthenticateFailBirthDateMismatch() {
        SimpleLoginRequestDTO request = new SimpleLoginRequestDTO();
        request.setCpf("123");
        request.setNomeCompleto("João Silva");
        request.setDataNascimento(LocalDate.of(2000, 1, 1));

        Employee emp = new Employee();
        emp.setName("João Silva");
        emp.setBirthDate(LocalDate.of(1990, 1, 1));

        when(employeeRepository.findAllByCpf("123")).thenReturn(List.of(emp));

        SimpleLoginResponseDTO response = simpleLoginService.authenticate(request);

        assertFalse(response.isSuccess());
        assertEquals("Dados de acesso incorretos", response.getMessage());
    }
}
