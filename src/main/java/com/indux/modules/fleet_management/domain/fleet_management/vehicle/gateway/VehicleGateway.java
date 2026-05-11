package com.indux.modules.fleet_management.domain.fleet_management.vehicle.gateway;

import com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleGateway {

    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(String id);

    Optional<Vehicle> findByPlate(String plate);

    List<Vehicle> findAll();

    void deleteById(String id);
}
