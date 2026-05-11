package com.indux.modules.employee_history.domain.repository;

import com.indux.modules.employee_history.application.dto.FilterSalaryComposition;
import com.indux.modules.employee_history.application.dto.SalaryCompositionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SalaryCompositionRepositoryCustom {
    Page<SalaryCompositionResponse> findByFilter(FilterSalaryComposition filter, Pageable pageable);
}
