package com.indux.core.domain.repository.generic;

import com.indux.core.application.dto.cbo.FilterCBO;
import com.indux.core.domain.model.employee.Cargo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CargoRepositoryCustom {
    Page<Cargo> findFilterCargo(FilterCBO filter, Pageable pageable);
}