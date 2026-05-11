package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.domain.entities.mongo.EquipamentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EquipamentRepositoryCustom {
    Page<EquipamentEntity> findByFilters(Boolean status, String manufacturerId, String propertiesId, String searchTerm, Pageable pageable);
}