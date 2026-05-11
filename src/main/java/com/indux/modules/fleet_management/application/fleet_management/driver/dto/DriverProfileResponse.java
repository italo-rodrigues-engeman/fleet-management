package com.indux.modules.fleet_management.application.fleet_management.driver.dto;

import com.indux.modules.fleet_management.domain.fleet_management.driver.enums.CnhCategory;
import com.indux.modules.fleet_management.domain.fleet_management.driver.enums.DriverStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DriverProfileResponse(

        String id,
        String employeeId,
        String employeeNumber,
        String cnh,
        CnhCategory cnhCategory,
        LocalDate cnhExpirationDate,
        DriverStatus status,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
