package com.indux.modules.employee_history.domain.repository;

import com.indux.modules.employee_history.application.dto.FilterHistory;
import com.indux.modules.employee_history.domain.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HistoryRepositoryCustom {
    Page<History> findFilterHistory(FilterHistory filter, Pageable pageable);

    Page<String> findDistinctEvents(Pageable pageable);
}
