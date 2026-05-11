package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.aplication.dtos.CalibrationFilter;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardFilter;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CalibrationStandardCustom {
    Page<CalibrationStandardEntity> findAllFilter(Pageable pageable, CalibrationStandardFilter filter);
}