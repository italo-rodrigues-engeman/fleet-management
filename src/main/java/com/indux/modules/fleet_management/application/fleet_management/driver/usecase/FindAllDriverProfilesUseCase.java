package com.indux.modules.fleet_management.application.fleet_management.driver.usecase;

import com.indux.modules.fleet_management.application.fleet_management.driver.dto.DriverProfileResponse;
import com.indux.modules.fleet_management.application.fleet_management.driver.mapper.DriverProfileApplicationMapper;
import com.indux.modules.fleet_management.domain.fleet_management.driver.gateway.DriverProfileGateway;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindAllDriverProfilesUseCase {

    private final DriverProfileGateway driverProfileGateway;

    public FindAllDriverProfilesUseCase(DriverProfileGateway driverProfileGateway){
        this.driverProfileGateway = driverProfileGateway;
    }

    public List<DriverProfileResponse> execute(){
        return driverProfileGateway.findAll()
                .stream()
                .map(DriverProfileApplicationMapper::toResponse)
                .toList();
    }

}
