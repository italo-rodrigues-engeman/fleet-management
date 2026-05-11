package com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase;

import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.CreateVehicleRequest;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.VehicleResponse;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.mapper.VehicleApplicationMapper;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity.Vehicle;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.enums.VehicleStatus;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.gateway.VehicleGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateVehicleUseCase {

    private final VehicleGateway vehicleGateway;

    public VehicleResponse execute(CreateVehicleRequest request) {

        if (request.plate() == null || request.plate().isBlank()) {
            throw new RuntimeException("Placa é obrigatória");
        }

        vehicleGateway.findByPlate(request.plate())
                .ifPresent(vehicle -> {
                    throw new RuntimeException("Já existe veículo cadastrado com essa placa");
                });

        Vehicle vehicle = new Vehicle();

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

        vehicle.setStatus(VehicleStatus.ACTIVE);

        Vehicle savedVehicle = vehicleGateway.save(vehicle);

        return VehicleApplicationMapper.toResponse(savedVehicle);
    }
}