package com.indux.modules.budgets.domain.repository;

import com.indux.modules.budgets.application.dto.SimpleBudgetFilter;
import com.indux.modules.budgets.domain.model.SimpleBudget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SimpleBudgetRepositoryCustom {
    Page<SimpleBudget> search(SimpleBudgetFilter filter, Pageable pageable);
}
