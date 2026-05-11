package com.indux.modules.training.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.training.application.dto.TrainingRequest;
import com.indux.modules.training.application.dto.TrainingResponse;
import com.indux.modules.training.application.service.TrainingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training")
@Validated
public class TrainingController {
    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping(consumes ={"multipart/form-data"})
    public ResponseEntity<GenericMessage> createTraining(
            @Valid @ModelAttribute @NonNull TrainingRequest dto
    ){
        trainingService.createTraining(dto);
        return ResponseEntity.ok(new GenericMessage("Treinamento Criado com Sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public Page<TrainingResponse> getTrainings(
            Pageable pageable,
            @RequestParam(required = false) List<String> filialHCM,
            @RequestParam(required = false)String search,
            @RequestParam(required = false)String obrigatoriedade,
            @RequestParam(required = false)String instituicaoId
    ){
        return trainingService.getTrainingsWithFilters(filialHCM, search, obrigatoriedade, instituicaoId, pageable);
    }

    @GetMapping("/{id}")
    public TrainingResponse getTrainingById(
            @PathVariable String id
    ){
        return trainingService.getTraining(id);
    }

    @PutMapping(value="/{id}", consumes ={"multipart/form-data"})
    public ResponseEntity<GenericMessage> updateTraining(
            @PathVariable String id,
            @Valid @ModelAttribute @NonNull TrainingRequest dto
    ){
        trainingService.updateTraining(id, dto);
        return ResponseEntity.ok(new GenericMessage("Treinamento Editado com Sucesso", HttpStatus.OK.value()));

    }
}
