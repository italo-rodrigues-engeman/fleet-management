package com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase;

import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.VehicleResponse;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.mapper.VehicleApplicationMapper;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity.Vehicle;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.gateway.VehicleGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindVehicleByPlateUseCase {

    private final VehicleGateway vehicleGateway;

    public VehicleResponse execute(String plate) {
        Vehicle vehicle = vehicleGateway.findByPlate(plate)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        return VehicleApplicationMapper.toResponse(vehicle);
    }

}