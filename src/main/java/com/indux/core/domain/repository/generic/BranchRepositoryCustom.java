package com.indux.core.domain.repository.generic;

import com.indux.core.domain.model.employee.Filial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BranchRepositoryCustom {
    Page<Filial> findWithSearch(String search, Pageable pageable);
}
