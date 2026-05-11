package com.indux.modules.flash_fuel.domain.repository;

import com.indux.modules.flash_fuel.domain.entities.log.FlashFuelLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashFuelLogRepository extends MongoRepository<FlashFuelLog, String> {
}
