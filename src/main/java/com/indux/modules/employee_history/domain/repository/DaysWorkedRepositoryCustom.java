package com.indux.modules.employee_history.domain.repository;

import com.indux.modules.employee_history.application.dto.CompetenceFilialProjection;
import com.indux.modules.employee_history.application.dto.FilterDaysWorked;
import com.indux.modules.employee_history.domain.entity.DaysWorked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface DaysWorkedRepositoryCustom {
    Page<DaysWorked> findFilterDaysWorked(FilterDaysWorked filter, Pageable pageable);

    Page<LocalDate> findDistinctCompetences(Pageable pageable);

    Page<CompetenceFilialProjection> findDistinctCompetencesByFilial(FilterDaysWorked filter, Pageable pageable);
}
