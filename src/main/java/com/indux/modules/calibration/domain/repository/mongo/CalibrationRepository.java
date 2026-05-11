package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.domain.entities.mongo.CalibrationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalibrationRepository extends MongoRepository<CalibrationEntity, String> {
}
