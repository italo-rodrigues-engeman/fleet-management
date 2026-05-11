package com.indux.modules.alpar.application.usecase;

import com.indux.modules.alpar.application.dto.request.EmployeeFilter;
import com.indux.modules.alpar.application.dto.response.EmployeeWithDependentsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetFilteredEmployeesUseCase {

    Page<EmployeeWithDependentsResponse> execute(EmployeeFilter filter, Pageable pageable);
}
