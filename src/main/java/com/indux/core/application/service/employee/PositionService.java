package com.indux.core.application.service.employee;

import com.indux.core.domain.model.employee.EmployeePosition;
import com.indux.core.domain.repository.employee.EmployeePositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PositionService {
    private final EmployeePositionRepository repository;

    public PositionService(EmployeePositionRepository repository) {
        this.repository = repository;
    }

    public Page<EmployeePosition> fetchAll(Pageable pageable){
        return repository.findAll(pageable);
    }

    public Page<EmployeePosition> fetchAllWithoutRepetition(Pageable pageable){
        return repository.findAllDistinctByName(pageable);
    }

    public Page<EmployeePosition> filterByName(String name, Pageable pageable){
        return repository.findDistinctByNameLikeIgnoreCase(name, pageable);
    }
}
