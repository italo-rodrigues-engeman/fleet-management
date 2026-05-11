package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import com.indux.modules.calibration.domain.entities.mongo.MeasuresEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalibrationStandardRepository extends MongoRepository<CalibrationStandardEntity, String>, CalibrationStandardCustom {
    Page<CalibrationStandardEntity> findAll(Pageable pageable);

    @Query("{'calibration._id': ?0}")
    Optional<CalibrationStandardEntity> findByCalibrationId(String calibrationId);
}