package com.indux.modules.calibration.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardAll;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardCreate;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardFilter;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardReturn;
import com.indux.modules.calibration.aplication.service.CalibrationStandardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gc/standard")
public class CalibrationStandardController {
    private final CalibrationStandardService calibrationStandardService;

    public CalibrationStandardController(CalibrationStandardService calibrationStandardService) {
        this.calibrationStandardService = calibrationStandardService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<GenericMessage> createCalibration(
            @ModelAttribute CalibrationStandardCreate calibration) {
        calibrationStandardService.createCalibrationStandard(calibration);
        return ResponseEntity.ok(new GenericMessage("Padrão de calibração criado com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<Page<CalibrationStandardAll>> getAllCalibrationStandards(
            Pageable pageable,
            @RequestParam(required = false)String search,
            @RequestParam(required = false)List<String> fabricanteId,
            @RequestParam(required = false)List<String> equipamentoId,
            @RequestParam(required = false)List<String> niMega,
            @RequestParam(required = false)List<String> unidadeId,
            @RequestParam(required = false)List<String> propriedadeId,
            @RequestParam(required = false)List<Integer> tempo,
            @RequestParam(required = false)Boolean status
    ) {
        CalibrationStandardFilter filter = new CalibrationStandardFilter(search,fabricanteId,equipamentoId,niMega,unidadeId,propriedadeId,tempo,status);
        return ResponseEntity.ok(calibrationStandardService.getAllCalibrationStandards(pageable,filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalibrationStandardReturn> getCalibrationStandard(
            @PathVariable String id
    ){
        return ResponseEntity.ok(calibrationStandardService.getCalibrationStandardsById(id));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericMessage> updateCalibrationStandard(
        @PathVariable String id,
        @ModelAttribute CalibrationStandardCreate calibration
    ){
        calibrationStandardService.updateCalibrationStandard(id, calibration);
        return ResponseEntity.ok(new GenericMessage("Padrão de Calibração editado com sucesso", HttpStatus.OK.value()));
    }
}