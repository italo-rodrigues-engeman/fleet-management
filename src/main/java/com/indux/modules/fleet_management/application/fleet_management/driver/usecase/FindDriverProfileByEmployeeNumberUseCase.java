package com.indux.modules.fleet_management.application.fleet_management.driver.usecase;

import com.indux.modules.fleet_management.application.fleet_management.driver.dto.DriverProfileResponse;
import com.indux.modules.fleet_management.application.fleet_management.driver.mapper.DriverProfileApplicationMapper;
import com.indux.modules.fleet_management.domain.fleet_management.driver.entity.DriverProfile;
import com.indux.modules.fleet_management.domain.fleet_management.driver.gateway.DriverProfileGateway;

import org.springframework.stereotype.Service;

@Service

public class FindDriverProfileByEmployeeNumberUseCase {

    private final DriverProfileGateway driverProfileGateway;

    public FindDriverProfileByEmployeeNumberUseCase(DriverProfileGateway driverProfileGateway){
        this.driverProfileGateway = driverProfileGateway;
    }

    public DriverProfileResponse execute(String employeeNumber){
        DriverProfile driverProfile = driverProfileGateway.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));

        return DriverProfileApplicationMapper.toResponse(driverProfile);
    }
}
