package com.indux.modules.fleet_management.application.fleet_management.vehicle.mapper;

import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.VehicleResponse;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity.Vehicle;

public class VehicleApplicationMapper {

    public static VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getPlate(),
                vehicle.getChassis(),
                vehicle.getRenavam(),
                vehicle.getManufactureYear(),
                vehicle.getModelYear(),
                vehicle.getCategory(),
                vehicle.getDetailedDescription(),
                vehicle.getCpfCnpj(),
                vehicle.getContract(),
                vehicle.getProject(),
                vehicle.getRegional(),
                vehicle.getUf(),
                vehicle.getActiveEngeman(),
                vehicle.getInsurance(),

                vehicle.getStatus(),

                vehicle.getManagerEmployeeId(),
                vehicle.getCurrentDriverId()
        );
    }
}