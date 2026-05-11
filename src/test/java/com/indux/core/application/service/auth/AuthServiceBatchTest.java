package com.indux.core.application.service.auth;

import com.indux.core.application.dto.auth.*;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.auth.UserRole;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.repository.user.UserRoleRepository;
import com.indux.core.domain.service.employee.EmployeeLookupService;
import com.indux.core.infra.exception.user.ValidatePasswordFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceBatchTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository roleRepository;

    @Mock
    private EmployeeLookupService employeeLookupService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRole defaultRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        defaultRole = new UserRole(1L, "USUÁRIO");
    }

    @Test
    @DisplayName("Should register users in batch successfully")
    void shouldRegisterUsersInBatchSuccessfully() {
        RegisterBatchRequest request = new RegisterBatchRequest(
                List.of("23023", "32320"),
                "Test@123");

        Employee emp1 = createEmployee("23023", "Alan Imbeloni", "12345678901", "11987654321");
        Employee emp2 = createEmployee("32320", "Maria Silva", "98765432109", "11912345678");

        when(employeeLookupService.findByRegistrations(anyList()))
                .thenReturn(List.of(emp1, emp2));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.findByCpf(anyString())).thenReturn(null);

        User savedUser1 = createUser(UUID.randomUUID(), "ALAN IMBELONI", "12345678901");
        User savedUser2 = createUser(UUID.randomUUID(), "MARIA SILVA", "98765432109");
        when(userRepository.saveAll(anyList())).thenReturn(List.of(savedUser1, savedUser2));

        RegisterBatchResponse response = authService.registerBatch(request);

        assertEquals(2, response.created().size());
        assertEquals(0, response.skipped().size());
        verify(userRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should skip employee not found")
    void shouldSkipEmployeeNotFound() {
        RegisterBatchRequest request = new RegisterBatchRequest(
                List.of("99999"),
                "Test@123");

        when(employeeLookupService.findByRegistrations(anyList()))
                .thenReturn(List.of());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(defaultRole));

        RegisterBatchResponse response = authService.registerBatch(request);

        assertEquals(0, response.created().size());
        assertEquals(1, response.skipped().size());
        assertEquals(SkippedUserItem.SkipReason.NOT_FOUND, response.skipped().get(0).reason());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should skip employee with existing CPF")
    void shouldSkipEmployeeWithExistingCPF() {
        RegisterBatchRequest request = new RegisterBatchRequest(
                List.of("23023"),
                "Test@123");

        Employee emp = createEmployee("23023", "Alan Imbeloni", "12345678901", "11987654321");
        User existingUser = createUser(UUID.randomUUID(), "ALAN IMBELONI", "12345678901");

        when(employeeLookupService.findByRegistrations(anyList())).thenReturn(List.of(emp));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(defaultRole));
        when(userRepository.findByCpf("12345678901")).thenReturn(existingUser);

        RegisterBatchResponse response = authService.registerBatch(request);

        assertEquals(0, response.created().size());
        assertEquals(1, response.skipped().size());
        assertEquals(SkippedUserItem.SkipReason.ALREADY_EXISTS, response.skipped().get(0).reason());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should skip employee with invalid data")
    void shouldSkipEmployeeWithInvalidData() {
        RegisterBatchRequest request = new RegisterBatchRequest(
                List.of("23023"),
                "Test@123");

        Employee emp = createEmployee("23023", "Alan Imbeloni", null, "11987654321");

        when(employeeLookupService.findByRegistrations(anyList())).thenReturn(List.of(emp));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(defaultRole));

        RegisterBatchResponse response = authService.registerBatch(request);

        assertEquals(0, response.created().size());
        assertEquals(1, response.skipped().size());
        assertEquals(SkippedUserItem.SkipReason.INVALID_DATA, response.skipped().get(0).reason());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should validate empty matriculas list")
    void shouldValidateEmptyMatriculasList() {
        RegisterBatchRequest request = new RegisterBatchRequest(
                List.of(),
                "Test@123");

        assertThrows(ValidatePasswordFailure.class, () -> authService.registerBatch(request));
    }

    @Test
    @DisplayName("Should skip employee when email generation fails (name is null)")
    void shouldSkipEmployeeWhenEmailGenerationFails() {
        RegisterBatchRequest request = new RegisterBatchRequest(
                List.of("23023"),
                "Test@123");

        Employee emp = createEmployee("23023", null, "12345678901", "11987654321");

        when(employeeLookupService.findByRegistrations(anyList())).thenReturn(List.of(emp));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(defaultRole));

        RegisterBatchResponse response = authService.registerBatch(request);

        assertEquals(0, response.created().size());
        assertEquals(1, response.skipped().size());
        assertEquals(SkippedUserItem.SkipReason.INVALID_DATA, response.skipped().get(0).reason());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should use cellphone2 when cellphone is invalid or empty")
    void shouldUseCellphone2WhenCellphoneIsInvalid() {
        RegisterBatchRequest request = new RegisterBatchRequest(
                List.of("23023"),
                "Test@123");

        Employee emp = createEmployee("23023", "Alan Imbeloni", "12345678901", ""); // cellphone empty
        emp.setCellphone2("11987654321");

        when(employeeLookupService.findByRegistrations(anyList())).thenReturn(List.of(emp));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.findByCpf("12345678901")).thenReturn(null);

        User savedUser = createUser(UUID.randomUUID(), "ALAN IMBELONI", "12345678901");

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<User>> usersCaptor = org.mockito.ArgumentCaptor.forClass((Class) List.class);
        when(userRepository.saveAll(usersCaptor.capture())).thenReturn(List.of(savedUser));

        authService.registerBatch(request);

        List<User> savedUsers = usersCaptor.getValue();
        assertEquals(1, savedUsers.size());
        assertEquals(11987654321L, savedUsers.get(0).getCellphone());
    }

    private Employee createEmployee(String registration, String name, String cpf, String phone) {
        Employee emp = new Employee();
        emp.setId(UUID.randomUUID());
        emp.setRegistration(registration);
        emp.setName(name);
        emp.setCpf(cpf);
        emp.setCellphone(phone);
        emp.setStatusEmployee("Trabalhando");
        return emp;
    }

    private User createUser(UUID id, String name, String cpf) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setCpf(cpf);
        user.setEmail(name.toLowerCase().replace(" ", ".") + "@kogni.com.br");
        user.setRole(defaultRole);
        return user;
    }
}
