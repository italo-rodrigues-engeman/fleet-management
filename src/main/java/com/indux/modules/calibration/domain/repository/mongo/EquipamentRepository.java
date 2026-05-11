package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.domain.entities.mongo.EquipamentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipamentRepository extends MongoRepository<EquipamentEntity, String>, EquipamentRepositoryCustom {
    Page<EquipamentEntity> findAll(Pageable pageable);
}