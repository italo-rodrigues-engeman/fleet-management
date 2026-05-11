package com.indux.modules.advance_suppliers.domain.repository;

import com.indux.modules.advance_suppliers.domain.dto.AdvanceSuppliersFilter;
import com.indux.modules.advance_suppliers.domain.entities.AdvanceSuppliers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomAdvanceSuppliersRepository {
    Page<AdvanceSuppliers> findByFilter(AdvanceSuppliersFilter filter, Pageable pageable);

}
