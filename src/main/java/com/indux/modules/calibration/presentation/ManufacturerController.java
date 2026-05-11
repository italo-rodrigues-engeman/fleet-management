package com.indux.modules.calibration.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.calibration.aplication.dtos.Manufacturer;
import com.indux.modules.calibration.aplication.service.ManufacturerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gc/manufacturer")
public class ManufacturerController {
    private final ManufacturerService manufacturerService;

    public ManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

    @PostMapping
    public ResponseEntity<GenericMessage> createContract(
            @RequestBody Manufacturer manufacturer
            ){
        manufacturerService.createManufacturer(manufacturer);
        return ResponseEntity.ok(new GenericMessage("Fabricante criado com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<Page<Manufacturer>> getManufacturers(
            @RequestParam(required = false) Boolean status,
            @RequestParam(value = "search", required = false) Object search,
            Pageable  pageable
    ){
        if (search != null) {
            return ResponseEntity.ok(manufacturerService.searchManufacturers(pageable, search, status));
        }
        return ResponseEntity.ok(manufacturerService.getAllManufacturers(status,pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericMessage> updateContract(
            @PathVariable String id,
            @RequestBody Manufacturer manufacturer
    ){
        manufacturerService.editManufacturer(id, manufacturer);
        return ResponseEntity.ok(new GenericMessage("Fabricante atualizado com sucesso", HttpStatus.OK.value()));
    }
}
