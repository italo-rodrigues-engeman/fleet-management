package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.aplication.dtos.CalibrationFilter;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardFilter;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import com.indux.modules.calibration.domain.entities.mongo.NiOrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NiOrganizationRepositoryCustom {
    Page<NiOrganizationEntity> findAllFilter(Pageable pageable, CalibrationFilter filter);

    Page<NiOrganizationEntity> findBySearchTermAndCalibrationIdCustom(String propriedadeId, String searchTerm, Pageable pageable);
}