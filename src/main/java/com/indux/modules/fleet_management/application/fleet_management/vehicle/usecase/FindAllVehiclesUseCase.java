package com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase;

import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.VehicleResponse;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.mapper.VehicleApplicationMapper;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity.Vehicle;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.gateway.VehicleGateway;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FindAllVehiclesUseCase {

    private final VehicleGateway vehicleGateway;

    public FindAllVehiclesUseCase(VehicleGateway vehicleGateway){
        this.vehicleGateway = vehicleGateway;
    }

    public List<VehicleResponse> execute() {
        return vehicleGateway.findAll()
                .stream()
                .map(VehicleApplicationMapper::toResponse)
                .toList();
    }

}
