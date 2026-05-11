package com.indux.modules.fleet_management.application.fleet_management.driver.usecase;

import com.indux.modules.fleet_management.application.fleet_management.driver.dto.DriverProfileResponse;
import com.indux.modules.fleet_management.application.fleet_management.driver.mapper.DriverProfileApplicationMapper;
import com.indux.modules.fleet_management.domain.fleet_management.driver.entity.DriverProfile;
import com.indux.modules.fleet_management.domain.fleet_management.driver.gateway.DriverProfileGateway;
import org.springframework.stereotype.Service;

@Service
public class FindDriverProfileByIdUseCase {

    private final DriverProfileGateway driverProfileGateway;

    public FindDriverProfileByIdUseCase(
            DriverProfileGateway driverProfileGateway
    ) {
        this.driverProfileGateway = driverProfileGateway;
    }

    public DriverProfileResponse execute(String id) {

        DriverProfile driverProfile = driverProfileGateway
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("ID não encontrado")
                );

        return DriverProfileApplicationMapper.toResponse(driverProfile);
    }
}