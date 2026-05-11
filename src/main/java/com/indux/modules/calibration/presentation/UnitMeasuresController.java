package com.indux.modules.calibration.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.calibration.aplication.dtos.UnitMeasures;
import com.indux.modules.calibration.aplication.service.UnitMeasuresService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gc/unit")
public class UnitMeasuresController {
    private final UnitMeasuresService unitMeasuresService;

    public UnitMeasuresController(UnitMeasuresService unitMeasuresService) {
        this.unitMeasuresService = unitMeasuresService;
    }

    @PostMapping
    public ResponseEntity<GenericMessage> createUnitMeasures(
            @RequestBody UnitMeasures unitMeasures
            ){
        unitMeasuresService.createUnitMeasures(unitMeasures);
        return ResponseEntity.ok(new GenericMessage("Unidade de medida criada com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<Page<UnitMeasures>> getUnitMeasures(
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String medidaId,
            @RequestParam(value = "search", required = false) Object search,
            Pageable  pageable
    ){
        return ResponseEntity.ok(unitMeasuresService.searchUnitMeasures(pageable, search, status, medidaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericMessage> updateUnitMeasures(
            @PathVariable String id,
            @RequestBody UnitMeasures unitMeasures
    ){
        unitMeasuresService.editUnitMeasures(id, unitMeasures);
        return ResponseEntity.ok(new GenericMessage("Unidade de medida atualizada com sucesso", HttpStatus.OK.value()));
    }
}