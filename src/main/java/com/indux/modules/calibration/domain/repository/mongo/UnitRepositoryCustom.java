package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.domain.entities.mongo.UnitMeasuresEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UnitRepositoryCustom {
    Page<UnitMeasuresEntity> findByFilters(Boolean status, String measureId, String searchTerm, Pageable pageable);
}