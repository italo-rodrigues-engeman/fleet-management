package com.indux.modules.fleet_management.application.fleet_management.vehicle.dto;

public record CreateVehicleRequest(
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
        String managerEmployeeId,
        String currentDriverId
) {
}