package com.indux.modules.alpar.presentation;

import com.indux.modules.alpar.application.dto.request.EmployeeFilter;
import com.indux.modules.alpar.application.dto.response.EmployeeWithDependentsResponse;
import com.indux.modules.alpar.application.usecase.GetEmployeesWithDependentsUseCase;
import com.indux.modules.alpar.application.usecase.GetFilteredEmployeesUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funcionarios")
@Validated
public class AlparController {

    private final GetEmployeesWithDependentsUseCase getEmployeesWithDependentsUseCase;
    private final GetFilteredEmployeesUseCase getFilteredEmployeesUseCase;

    public AlparController(
            GetEmployeesWithDependentsUseCase getEmployeesWithDependentsUseCase,
            GetFilteredEmployeesUseCase getFilteredEmployeesUseCase) {
        this.getEmployeesWithDependentsUseCase = getEmployeesWithDependentsUseCase;
        this.getFilteredEmployeesUseCase = getFilteredEmployeesUseCase;
    }

    @GetMapping
    public Page<EmployeeWithDependentsResponse> getEmployeesWithDependents(Pageable pageable) {
        return getEmployeesWithDependentsUseCase.execute(pageable);
    }

    @PostMapping("/filter")
    public Page<EmployeeWithDependentsResponse> getFilteredEmployees(@RequestBody EmployeeFilter filter,
            Pageable pageable) {
        return getFilteredEmployeesUseCase.execute(filter, pageable);
    }
}
