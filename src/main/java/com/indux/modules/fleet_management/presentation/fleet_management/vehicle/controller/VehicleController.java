package com.indux.modules.fleet_management.presentation.fleet_management.vehicle.controller;

import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.CreateVehicleRequest;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.UpdateVehicleRequest;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.VehicleResponse;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase.CreateVehicleUseCase;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase.FindAllVehiclesUseCase;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase.FindVehicleByIdUseCase;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase.FindVehicleByPlateUseCase;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase.UpdateVehicleUseCase;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase.InactivateVehicleUseCase;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase.ImportVehiclesUseCase;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final FindAllVehiclesUseCase findAllVehiclesUseCase;
    private final FindVehicleByIdUseCase findVehicleByIdUseCase;
    private final FindVehicleByPlateUseCase findVehicleByPlateUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final InactivateVehicleUseCase inactivateVehicleUseCase;
    private final ImportVehiclesUseCase importVehiclesUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse create(@RequestBody CreateVehicleRequest request) {
        return createVehicleUseCase.execute(request);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public List<VehicleResponse> importVehicles(@RequestParam("file") MultipartFile file) {
        return importVehiclesUseCase.execute(file);
    }

    @GetMapping
    public List<VehicleResponse> findAll() {
        return findAllVehiclesUseCase.execute();
    }

    @GetMapping("/{id}")
    public VehicleResponse findById(@PathVariable String id) {
        return findVehicleByIdUseCase.execute(id);
    }

    @GetMapping("/plate/{plate}")
    public VehicleResponse findByPlate(@PathVariable String plate) {
        return findVehicleByPlateUseCase.execute(plate);
    }

    @PutMapping("/{id}")
    public VehicleResponse update(
            @PathVariable String id,
            @RequestBody UpdateVehicleRequest request
    ) {
        return updateVehicleUseCase.execute(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inactivate(@PathVariable String id) {
        inactivateVehicleUseCase.execute(id);
    }


}