package com.indux.modules.calibration.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.calibration.aplication.dtos.CalibrationAll;
import com.indux.modules.calibration.aplication.dtos.CalibrationCreate;
import com.indux.modules.calibration.aplication.dtos.CalibrationFilter;
import com.indux.modules.calibration.aplication.service.CalibrationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/gc/calibration")
@Validated
public class CalibrationController {
    private final CalibrationService calibrationService;

    public CalibrationController(CalibrationService calibrationService) {
        this.calibrationService = calibrationService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<GenericMessage> createCalibration(
            @Valid @ModelAttribute @NonNull CalibrationCreate calibrationCreate
            ) {
            calibrationService.createCalibration(calibrationCreate);
            return ResponseEntity.ok(new GenericMessage("Calibração criada com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<Page<CalibrationAll>> getAllCalibrationStandards(
            @NonNull Pageable pageable,
            @RequestParam(required = false) List<String> fabricanteId,
            @RequestParam(required = false)List<String> equipamentoId,
            @RequestParam(required = false)List<String> niMega,
            @RequestParam(required = false)List<String> unidadeId,
            @RequestParam(required = false)List<String> propriedadeId,
            @RequestParam(required = false)List<Integer> tempo,
            @RequestParam(required = false)List<Long> regionalId,
            @RequestParam(required = false)List<Long> contratoId,
            @RequestParam(required = false)List<Long> projetoId,
            @RequestParam(required = false)List<String> statusCalibracao,
            @RequestParam(required = false)Boolean situacao,
            @RequestParam(required = false)Boolean atrasado
    ) {
        CalibrationFilter filter = new CalibrationFilter(fabricanteId,equipamentoId,niMega,unidadeId,propriedadeId,tempo, regionalId, contratoId, projetoId, statusCalibracao, situacao,atrasado);
        return ResponseEntity.ok(calibrationService.getAllCalibration(pageable,filter));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericMessage> updateCalibrationStandard(
            @PathVariable @NonNull String id,
            @Valid @ModelAttribute @NonNull CalibrationCreate calibration
    ){
        calibrationService.updateCalibration(id, calibration);
        return ResponseEntity.ok(new GenericMessage("Calibração editado com sucesso", HttpStatus.OK.value()));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<GenericMessage> deleteCalibrationStandard(
            @PathVariable String id,
            JwtAuthenticationToken jwt
    ){
        var name = jwt.getToken().getClaims().get("name");
        calibrationService.deleteCalibration(id, name);
        return ResponseEntity.ok(new GenericMessage("Calibração deletada com sucesso", HttpStatus.OK.value()));
    }

}
