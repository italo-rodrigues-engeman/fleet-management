package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import com.indux.modules.calibration.domain.entities.mongo.MeasuresEntity;
import com.indux.modules.calibration.domain.entities.mongo.NiOrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NiOrganizationRepository extends MongoRepository<NiOrganizationEntity, String>, NiOrganizationRepositoryCustom {
    Page<NiOrganizationEntity> findAll(Pageable pageable);
    Page<NiOrganizationEntity> findByCalibrationStandard(String standard, Pageable pageable);
    @Query("{'calibration._id': ?0}")
    Optional<NiOrganizationEntity> findByCalibrationId(String calibrationId);
}