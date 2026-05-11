package com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase;

import com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity.Vehicle;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.gateway.VehicleGateway;
import org.springframework.stereotype.Service;

@Service
public class InactivateVehicleUseCase {

    private final VehicleGateway vehicleGateway;

    public InactivateVehicleUseCase(VehicleGateway vehicleGateway) {
        this.vehicleGateway = vehicleGateway;
    }

    public void execute(String id) {
        Vehicle vehicle = vehicleGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        vehicle.inactivate();

        vehicleGateway.save(vehicle);
    }

}
