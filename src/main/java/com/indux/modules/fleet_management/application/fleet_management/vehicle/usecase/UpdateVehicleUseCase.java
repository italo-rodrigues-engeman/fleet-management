package com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase;

import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.UpdateVehicleRequest;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.VehicleResponse;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.mapper.VehicleApplicationMapper;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity.Vehicle;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.gateway.VehicleGateway;
import org.springframework.stereotype.Service;

@Service
public class UpdateVehicleUseCase {

    private final VehicleGateway vehicleGateway;

    public UpdateVehicleUseCase(VehicleGateway vehicleGateway) {
        this.vehicleGateway = vehicleGateway;
    }

    public VehicleResponse execute(String id, UpdateVehicleRequest request) {
        Vehicle vehicle = vehicleGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        vehicle.setPlate(request.plate());
        vehicle.setChassis(request.chassis());
        vehicle.setRenavam(request.renavam());
        vehicle.setManufactureYear(request.manufactureYear());
        vehicle.setModelYear(request.modelYear());
        vehicle.setCategory(request.category());
        vehicle.setDetailedDescription(request.detailedDescription());

        vehicle.setCpfCnpj(request.cpfCnpj());
        vehicle.setContract(request.contract());
        vehicle.setProject(request.project());
        vehicle.setRegional(request.regional());
        vehicle.setUf(request.uf());
        vehicle.setActiveEngeman(request.activeEngeman());
        vehicle.setInsurance(request.insurance());

        vehicle.setManagerEmployeeId(request.managerEmployeeId());
        vehicle.setCurrentDriverId(request.currentDriverId());

        Vehicle updatedVehicle = vehicleGateway.save(vehicle);
        return VehicleApplicationMapper.toResponse(updatedVehicle);
    }
}