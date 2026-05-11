package com.indux.modules.calibration.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.calibration.aplication.dtos.Equipment;
import com.indux.modules.calibration.aplication.service.EquipmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gc/equipment")
public class EquipmentController {
    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    public ResponseEntity<GenericMessage> createEquipment(
            @RequestBody Equipment equipment
            ){
        equipmentService.createEquipment(equipment);
        return ResponseEntity.ok(new GenericMessage("Equipamento criado com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<Page<Equipment>> getEquipments(
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String manufacturerId,
            @RequestParam(required = false) String propertiesId,
            @RequestParam(value = "search", required = false) String search,
            Pageable  pageable
    ){
        return ResponseEntity.ok(equipmentService.getEquipments(status, manufacturerId, propertiesId, search, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericMessage> updateEquipment(
            @PathVariable String id,
            @RequestBody Equipment equipment
    ){
        equipmentService.editEquipment(id, equipment);
        return ResponseEntity.ok(new GenericMessage("Equipamento atualizado com sucesso", HttpStatus.OK.value()));
    }
}