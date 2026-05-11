package com.indux.modules.fleet_management.application.fleet_management.driver.usecase;


import com.indux.modules.fleet_management.application.fleet_management.driver.dto.CreateDriverProfileRequest;
import com.indux.modules.fleet_management.application.fleet_management.driver.dto.DriverProfileResponse;
import com.indux.modules.fleet_management.application.fleet_management.driver.mapper.DriverProfileApplicationMapper;
import com.indux.modules.fleet_management.domain.fleet_management.driver.entity.DriverProfile;
import com.indux.modules.fleet_management.domain.fleet_management.driver.enums.DriverStatus;
import com.indux.modules.fleet_management.domain.fleet_management.driver.gateway.DriverProfileGateway;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class CreateDriverProfileUseCase {

    private final DriverProfileGateway driverProfileGateway;

    public DriverProfileResponse execute(CreateDriverProfileRequest request){

        if(request.employeeNumber() == null|| request.employeeNumber().isBlank()){
            throw new RuntimeException("Matrícula é obrigatória");
        }

        driverProfileGateway.findByEmployeeNumber(request.employeeNumber())
                .ifPresent(driverProfile -> {
                    throw new RuntimeException("Já existe condutor cadastrado com essa matrícula");
                });

        DriverProfile driverProfile = new DriverProfile();

        driverProfile.setEmployeeId(request.employeeId());
        driverProfile.setEmployeeNumber(request.employeeNumber());
        driverProfile.setCnh(request.cnh());
        driverProfile.setCnhCategory(request.cnhCategory());
        driverProfile.setCnhExpirationDate(request.cnhExpirationDate());

        driverProfile.setStatus(
                request.status() !=  null ? request.status() : DriverStatus.ACTIVE
        );

        driverProfile.setActive(
                request.active() != null ? request.active() : true
        );

        driverProfile.setCreatedAt(LocalDateTime.now());
        driverProfile.setUpdatedAt(LocalDateTime.now());

        DriverProfile savedDriverProfile = driverProfileGateway.save(driverProfile);

        return DriverProfileApplicationMapper.toResponse(savedDriverProfile);

    }
}
