package com.indux.modules.alpar.application.usecase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.modules.alpar.application.dto.response.EmployeeWithDependentsResponse;
import com.indux.modules.alpar.application.mapper.EmployeeWithDependentsMapper;
import com.indux.modules.alpar.domain.Dependent;
import com.indux.modules.alpar.persistence.repository.EmployeeWithDependentsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class GetEmployeesWithDependentsService implements GetEmployeesWithDependentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetEmployeesWithDependentsService.class);
    private static final TypeReference<List<Dependent>> DEPENDENT_LIST_TYPE = new TypeReference<>() {
    };

    private final EmployeeWithDependentsRepository repository;
    private final EmployeeWithDependentsMapper mapper;
    private final ObjectMapper objectMapper;

    public GetEmployeesWithDependentsService(
            EmployeeWithDependentsRepository repository,
            EmployeeWithDependentsMapper mapper,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<EmployeeWithDependentsResponse> execute(Pageable pageable) {
        return repository.findAll(pageable).map(view -> {
            EmployeeWithDependentsResponse response = mapper.toResponse(view);
            response.setDependents(parseDependents(view.getDependentsJson()));
            return response;
        });
    }

    private List<Dependent> parseDependents(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, DEPENDENT_LIST_TYPE);
        } catch (Exception e) {
            log.warn("Failed to parse dependents JSON for employee: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
