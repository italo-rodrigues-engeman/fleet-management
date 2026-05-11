package com.indux.modules.fleet_management.application.fleet_management.driver.mapper;

import com.indux.modules.fleet_management.application.fleet_management.driver.dto.DriverProfileResponse;
import com.indux.modules.fleet_management.domain.fleet_management.driver.entity.DriverProfile;

public class DriverProfileApplicationMapper {
    public static DriverProfileResponse toResponse(DriverProfile driverProfile){
        return new DriverProfileResponse(
                driverProfile.getId(),
                driverProfile.getEmployeeId(),
                driverProfile.getEmployeeNumber(),
                driverProfile.getCnh(),
                driverProfile.getCnhCategory(),
                driverProfile.getCnhExpirationDate(),
                driverProfile.getStatus(),
                driverProfile.getActive(),
                driverProfile.getCreatedAt(),
                driverProfile.getUpdatedAt()
        );
    }
}
