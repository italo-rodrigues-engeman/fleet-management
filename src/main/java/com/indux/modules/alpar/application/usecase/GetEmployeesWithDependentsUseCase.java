package com.indux.modules.alpar.application.usecase;

import com.indux.modules.alpar.application.dto.response.EmployeeWithDependentsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetEmployeesWithDependentsUseCase {

    Page<EmployeeWithDependentsResponse> execute(Pageable pageable);
}
