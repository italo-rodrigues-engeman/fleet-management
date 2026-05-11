package com.indux.modules.fleet_management.domain.fleet_management.driver.gateway;

import com.indux.modules.fleet_management.domain.fleet_management.driver.entity.DriverProfile;

import java.util.List;
import java.util.Optional;

public interface DriverProfileGateway {

    DriverProfile save(DriverProfile driverProfile);

    Optional<DriverProfile> findById(String id);

    Optional<DriverProfile> findByEmployeeNumber(String employeeNumber);

    List<DriverProfile> findAll();

    void inactivate(String id);
}
