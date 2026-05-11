package com.indux.modules.union_registration.domain.repository;

import com.indux.modules.union_registration.application.dto.ListUnionsFilterDTO;
import com.indux.modules.union_registration.domain.model.Union;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UnionRepositoryCustom {
    
    Page<Union> findAllWithFilters(ListUnionsFilterDTO filters, Pageable pageable);
}

