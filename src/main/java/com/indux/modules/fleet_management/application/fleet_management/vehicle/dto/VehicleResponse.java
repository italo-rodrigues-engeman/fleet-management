package com.indux.modules.fleet_management.application.fleet_management.vehicle.dto;

import com.indux.modules.fleet_management.domain.fleet_management.vehicle.enums.VehicleStatus;

public record VehicleResponse (
        String id,
        String plate,
        String chassis,
        String renavam,
        Integer manufactureYear,
        Integer modelYear,
        String category,
        String detailedDescription,

        String cpfCnpj,
        String contract,
        String project,
        String regional,
        String uf,
        Boolean activeEngeman,
        Boolean insurance,

        VehicleStatus status,
        String managerEmployeeId,
        String currentDriverId
) {}