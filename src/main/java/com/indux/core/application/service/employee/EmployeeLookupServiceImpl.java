package com.indux.core.application.service.employee;

import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.domain.service.employee.EmployeeLookupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeLookupServiceImpl implements EmployeeLookupService {
    private final EmployeeRepository employeeRepository;

    public EmployeeLookupServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Optional<Employee> findByRegistration(String registration) {
        if (registration == null || registration.isBlank()) {
            return Optional.empty();
        }
        return employeeRepository.findByRegistration(registration);
    }

    @Override
    public List<Employee> findByRegistrations(List<String> registrations) {
        if (registrations == null || registrations.isEmpty()) {
            return List.of();
        }
        Set<Employee> employees = employeeRepository.findAllByRegistrationIn(registrations);
        return employees.stream().collect(Collectors.toList());
    }
}
