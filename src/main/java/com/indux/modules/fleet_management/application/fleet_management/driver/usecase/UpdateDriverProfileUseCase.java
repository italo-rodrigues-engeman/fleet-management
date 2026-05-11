package com.indux.modules.fleet_management.application.fleet_management.driver.usecase;

import com.indux.modules.fleet_management.application.fleet_management.driver.dto.DriverProfileResponse;
import com.indux.modules.fleet_management.application.fleet_management.driver.dto.UpdateDriverProfileRequest;
import com.indux.modules.fleet_management.application.fleet_management.driver.mapper.DriverProfileApplicationMapper;
import com.indux.modules.fleet_management.domain.fleet_management.driver.entity.DriverProfile;
import com.indux.modules.fleet_management.domain.fleet_management.driver.gateway.DriverProfileGateway;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UpdateDriverProfileUseCase {

    private final DriverProfileGateway driverProfileGateway;

    public UpdateDriverProfileUseCase(
            DriverProfileGateway driverProfileGateway
    ) {
        this.driverProfileGateway = driverProfileGateway;
    }

    public DriverProfileResponse execute(
            String id,
            UpdateDriverProfileRequest request
    ) {

        DriverProfile driverProfile = driverProfileGateway.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Condutor não encontrado")
                );

        if (request.cnh() != null) {
            driverProfile.setCnh(request.cnh());
        }

        if (request.cnhCategory() != null) {
            driverProfile.setCnhCategory(request.cnhCategory());
        }

        if (request.cnhExpirationDate() != null) {
            driverProfile.setCnhExpirationDate(
                    request.cnhExpirationDate()
            );
        }

        if (request.status() != null) {
            driverProfile.setStatus(request.status());
        }

        if (request.active() != null) {
            driverProfile.setActive(request.active());
        }

        driverProfile.setUpdatedAt(LocalDateTime.now());

        DriverProfile updatedDriverProfile =
                driverProfileGateway.save(driverProfile);

        return DriverProfileApplicationMapper
                .toResponse(updatedDriverProfile);
    }
}