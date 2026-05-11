package com.indux.modules.fleet_management.persistence.fleet_management.vehicle.repository;

import com.indux.modules.fleet_management.persistence.fleet_management.vehicle.model.VehicleModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VehicleRepository extends MongoRepository <VehicleModel, String>{

    Optional<VehicleModel> findByPlate(String plate);

}
