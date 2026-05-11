package com.indux.core.application.service.auth;

import com.indux.core.application.dto.auth.MobileLoginRequestDTO;
import com.indux.core.application.dto.auth.MobileLoginResponseDTO;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.infra.config.security.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobileLoginServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TokenProvider tokenProvider;

    private MobileLoginService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new MobileLoginService(employeeRepository, tokenProvider);
    }

    @Test
    @DisplayName("Deve priorizar matrícula ativa quando houver múltiplos vínculos para o mesmo CPF")
    void shouldPrioritizeActiveRegistrationForSameCpf() {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        Employee terminatedEmployee = buildEmployee(UUID.randomUUID(), "12345678900", "MAT-OLD", birthDate, LocalDate.of(2025, 1, 1), "Demitido");
        Employee activeEmployee = buildEmployee(UUID.randomUUID(), "12345678900", "MAT-ACTIVE", birthDate, LocalDate.of(2024, 1, 1), "Trabalhando");
        Employee inactiveEmployee = buildEmployee(UUID.randomUUID(), "12345678900", "MAT-INACTIVE", birthDate, LocalDate.of(2023, 1, 1), "Afastado");

        when(employeeRepository.findAllByCpf("12345678900")).thenReturn(List.of(terminatedEmployee, inactiveEmployee, activeEmployee));
        when(tokenProvider.generateToken(eq(activeEmployee.getId().toString()), anyList(), eq(Map.of("cpf", "12345678900", "matricula", "MAT-ACTIVE"))))
                .thenReturn("jwt-token");

        MobileLoginResponseDTO response = service.authenticate(new MobileLoginRequestDTO("123.456.789-00", birthDate));

        assertTrue(response.success());
        assertEquals("jwt-token", response.token());
        verify(employeeRepository).findAllByCpf("12345678900");
    }

    @Test
    @DisplayName("Deve falhar quando todos os vínculos do colaborador estiverem demitidos mesmo com variação no status")
    void shouldFailWhenAllEmployeesAreTerminated() {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        Employee terminatedEmployee = buildEmployee(UUID.randomUUID(), "12345678900", "MAT-01", birthDate, LocalDate.of(2024, 1, 1), "Demitido por justa causa");
        when(employeeRepository.findAllByCpf("12345678900")).thenReturn(List.of(terminatedEmployee));

        MobileLoginResponseDTO response = service.authenticate(new MobileLoginRequestDTO("12345678900", birthDate));

        assertFalse(response.success());
        assertEquals("Dados de acesso incorretos", response.message());
        assertNull(response.token());
    }

    @Test
    @DisplayName("Deve falhar quando data de nascimento estiver incorreta")
    void shouldFailWhenBirthDateDoesNotMatch() {
        Employee employee = buildEmployee(UUID.randomUUID(), "12345678900", "MAT-01", LocalDate.of(1990, 1, 1), LocalDate.of(2024, 1, 1), "Trabalhando");
        when(employeeRepository.findAllByCpf("12345678900")).thenReturn(List.of(employee));

        MobileLoginResponseDTO response = service.authenticate(new MobileLoginRequestDTO("12345678900", LocalDate.of(2000, 1, 1)));

        assertFalse(response.success());
        assertEquals("Dados de acesso incorretos", response.message());
        assertNull(response.token());
    }

    private Employee buildEmployee(UUID id, String cpf, String registration, LocalDate birthDate, LocalDate admissionDate, String status) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setCpf(cpf);
        employee.setRegistration(registration);
        employee.setBirthDate(birthDate);
        employee.setAdmissionDate(admissionDate);
        employee.setStatusEmployee(status);
        return employee;
    }
}
