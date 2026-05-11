package com.indux.modules.calibration.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.calibration.aplication.dtos.Measures;
import com.indux.modules.calibration.aplication.service.MeasuresService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gc/measures")
public class MeasuresController {
    private final MeasuresService measuresService;

    public MeasuresController(MeasuresService measuresService) {
        this.measuresService = measuresService;
    }

    @PostMapping
    public ResponseEntity<GenericMessage> createMeasures(
            @RequestBody Measures measures
            ){
        measuresService.createMeasures(measures);
        return ResponseEntity.ok(new GenericMessage("Medida criada com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<Page<Measures>> getMeasures(
            @RequestParam(required = false) Boolean status,
            @RequestParam(value = "search", required = false) Object search,
            Pageable  pageable
    ){
        if (search != null) {
            return ResponseEntity.ok(measuresService.searchMeasures(pageable, search, status));
        }
        return ResponseEntity.ok(measuresService.getAllMeasures(status,pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericMessage> updateMeasures(
            @PathVariable String id,
            @RequestBody Measures measures
    ){
        measuresService.editMeasures(id, measures);
        return ResponseEntity.ok(new GenericMessage("Medida atualizada com sucesso", HttpStatus.OK.value()));
    }
}