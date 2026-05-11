package com.indux.modules.employee_history.domain.repository;

import com.indux.modules.employee_history.application.dto.FilterPayroll;
import com.indux.modules.employee_history.application.dto.PayrollResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayrollRepositoryCustom {
    Page<PayrollResponse> findFilterPayroll(FilterPayroll filter, Pageable pageable);

    Page<String> findDistinctEventNames(String search, Pageable pageable);

    Page<String> findDistinctEventDescriptions(Pageable pageable);
}
