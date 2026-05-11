package com.indux.modules.fleet_management.persistence.fleet_management.vehicle.gateway;

import com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity.Vehicle;
import com.indux.modules.fleet_management.domain.fleet_management.vehicle.gateway.VehicleGateway;
import com.indux.modules.fleet_management.persistence.fleet_management.vehicle.mapper.VehiclePersistenceMapper;
import com.indux.modules.fleet_management.persistence.fleet_management.vehicle.model.VehicleModel;
import com.indux.modules.fleet_management.persistence.fleet_management.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class VehicleGatewayImpl implements VehicleGateway{

    private final VehicleRepository repository;

    public VehicleGatewayImpl(VehicleRepository repository){
        this.repository = repository;
    }

    @Override
    public Vehicle save(Vehicle vehicle){
        VehicleModel model = VehiclePersistenceMapper.toModel(vehicle);
        VehicleModel savedModel = repository.save(model);

        return VehiclePersistenceMapper.toDomain(savedModel);
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        return repository.findById(id)
                .map(VehiclePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByPlate(String plate) {
        return repository.findByPlate(plate)
                .map(VehiclePersistenceMapper::toDomain);
    }


    @Override
    public List<Vehicle> findAll() {
        return repository.findAll()
                .stream()
                .map(VehiclePersistenceMapper::toDomain)
                .toList();
    }


    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

}
