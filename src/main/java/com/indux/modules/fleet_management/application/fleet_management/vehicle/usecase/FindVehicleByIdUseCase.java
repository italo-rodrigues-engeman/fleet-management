package com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase;

import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.VehicleResponse;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.mapper.VehicleApplicationMapper;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity.Vehicle;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.gateway.VehicleGateway;
import org.springframework.stereotype.Service;

@Service

public class FindVehicleByIdUseCase {

    private final VehicleGateway vehicleGateway;

    public FindVehicleByIdUseCase(VehicleGateway vehicleGateway) {
        this.vehicleGateway = vehicleGateway;
    }

    public VehicleResponse execute(String id) {
        Vehicle vehicle = vehicleGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        return VehicleApplicationMapper.toResponse(vehicle);
    }

}
