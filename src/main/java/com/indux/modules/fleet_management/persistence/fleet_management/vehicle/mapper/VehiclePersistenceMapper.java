package com.indux.modules.fleet_management.persistence.fleet_management.vehicle.mapper;

import com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity.Vehicle;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.enums.VehicleStatus;
import com.indux.modules.fleet_management.persistence.fleet_management.vehicle.model.VehicleModel;

public class VehiclePersistenceMapper {

    public static VehicleModel toModel(Vehicle vehicle) {
        VehicleModel model = new VehicleModel();

        model.setId(vehicle.getId());
        model.setPlate(vehicle.getPlate());
        model.setChassis(vehicle.getChassis());
        model.setRenavam(vehicle.getRenavam());
        model.setManufactureYear(vehicle.getManufactureYear());
        model.setModelYear(vehicle.getModelYear());
        model.setCategory(vehicle.getCategory());
        model.setDetailedDescription(vehicle.getDetailedDescription());
        model.setCpfCnpj(vehicle.getCpfCnpj());
        model.setContract(vehicle.getContract());
        model.setProject(vehicle.getProject());
        model.setRegional(vehicle.getRegional());
        model.setUf(vehicle.getUf());
        model.setActiveEngeman(vehicle.getActiveEngeman());
        model.setInsurance(vehicle.getInsurance());

        if (vehicle.getStatus() != null) {
            model.setStatus(vehicle.getStatus().name());
        }

        model.setManagerEmployeeId(vehicle.getManagerEmployeeId());
        model.setCurrentDriverId(vehicle.getCurrentDriverId());

        return model;
    }

    public static Vehicle toDomain(VehicleModel model) {
        Vehicle vehicle = new Vehicle();

        vehicle.setId(model.getId());
        vehicle.setPlate(model.getPlate());
        vehicle.setChassis(model.getChassis());
        vehicle.setRenavam(model.getRenavam());
        vehicle.setManufactureYear(model.getManufactureYear());
        vehicle.setModelYear(model.getModelYear());
        vehicle.setCategory(model.getCategory());
        vehicle.setDetailedDescription(model.getDetailedDescription());
        vehicle.setCpfCnpj(model.getCpfCnpj());
        vehicle.setContract(model.getContract());
        vehicle.setProject(model.getProject());
        vehicle.setRegional(model.getRegional());
        vehicle.setUf(model.getUf());
        vehicle.setActiveEngeman(model.getActiveEngeman());
        vehicle.setInsurance(model.getInsurance());

        if (model.getStatus() != null) {
            vehicle.setStatus(VehicleStatus.valueOf(model.getStatus()));
        }

        vehicle.setManagerEmployeeId(model.getManagerEmployeeId());
        vehicle.setCurrentDriverId(model.getCurrentDriverId());

        return vehicle;
    }
}