package com.indux.core.application.service.auth;

import com.indux.core.application.dto.auth.MobileLoginRequestDTO;
import com.indux.core.application.dto.auth.MobileLoginResponseDTO;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.infra.config.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MobileLoginService {

    public static final String MOBILE_ACCESS_ROLE = "MOBILE_ACCESS";
    private static final String INVALID_CREDENTIALS_MESSAGE = "Dados de acesso incorretos";
    private static final String TERMINATED_STATUS_TOKEN = "DEMIT";
    private static final String ACTIVE_STATUS_TOKEN = "TRABALH";

    private final EmployeeRepository employeeRepository;
    private final TokenProvider tokenProvider;

    public MobileLoginResponseDTO authenticate(MobileLoginRequestDTO request) {
        String normalizedCpf = normalizeCpf(request.cpf());

        Optional<Employee> validatedEmployee = validateCredentials(normalizedCpf, request.dataNascimento());
        if (validatedEmployee.isEmpty()) {
            return MobileLoginResponseDTO.failure(INVALID_CREDENTIALS_MESSAGE);
        }

        Employee employee = validatedEmployee.get();
        String token = tokenProvider.generateToken(
                employee.getId().toString(),
                List.of(MOBILE_ACCESS_ROLE),
                Map.of(
                        "cpf", employee.getCpf(),
                        "matricula", employee.getRegistration()
                )
        );

        return MobileLoginResponseDTO.success(token);
    }

    private Optional<Employee> validateCredentials(String cpf, LocalDate birthDate) {
        return employeeRepository.findAllByCpf(cpf).stream()
                .filter(employee -> employee.getBirthDate() != null && employee.getBirthDate().equals(birthDate))
                .filter(this::isEligibleForLogin)
                .max(employeeSelectionComparator());
    }

    private boolean isEligibleForLogin(Employee employee) {
        return !containsStatusToken(employee, TERMINATED_STATUS_TOKEN);
    }

    private Comparator<Employee> employeeSelectionComparator() {
        return Comparator
                .comparing(this::isActiveEmployee)
                .thenComparing(employee -> Optional.ofNullable(employee.getAdmissionDate()).orElse(LocalDate.MIN));
    }

    private boolean isActiveEmployee(Employee employee) {
        return containsStatusToken(employee, ACTIVE_STATUS_TOKEN);
    }

    private boolean containsStatusToken(Employee employee, String token) {
        return normalizeStatus(employee.getStatusEmployee()).contains(token);
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }

        String normalizedStatus = Normalizer.normalize(status, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalizedStatus.trim().toUpperCase();
    }

    private String normalizeCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("\\D", "");
    }
}
