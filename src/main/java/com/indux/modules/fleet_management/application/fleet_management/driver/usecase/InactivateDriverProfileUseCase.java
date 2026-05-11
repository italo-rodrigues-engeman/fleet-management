package com.indux.modules.fleet_management.application.fleet_management.driver.usecase;

import com.indux.modules.fleet_management.domain.fleet_management.driver.entity.DriverProfile;
import com.indux.modules.fleet_management.domain.fleet_management.driver.gateway.DriverProfileGateway;
import org.springframework.stereotype.Service;

@Service
public class InactivateDriverProfileUseCase {

    private final DriverProfileGateway driverProfileGateway;

    public InactivateDriverProfileUseCase(
            DriverProfileGateway driverProfileGateway
    ) {
        this.driverProfileGateway = driverProfileGateway;
    }

    public void execute(String id) {

        DriverProfile driverProfile = driverProfileGateway.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Condutor não encontrado")
                );

        driverProfile.inactivate();

        driverProfileGateway.save(driverProfile);
    }
}