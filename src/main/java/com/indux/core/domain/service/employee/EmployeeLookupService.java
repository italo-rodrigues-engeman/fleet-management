package com.indux.core.domain.service.employee;

import com.indux.core.domain.model.employee.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeLookupService {
    Optional<Employee> findByRegistration(String registration);
    
    List<Employee> findByRegistrations(List<String> registrations);
}
