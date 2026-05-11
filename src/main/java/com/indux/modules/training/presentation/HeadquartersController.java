package com.indux.modules.training.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.training.application.dto.HeadquartesDTO;
import com.indux.modules.training.application.service.HeadquartersService;
import com.indux.modules.training.domain.entity.HeadquartersEntity;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/headquarters")
@Validated
public class HeadquartersController {
    private final HeadquartersService headquartersService;

    public HeadquartersController(HeadquartersService headquartersService) {
        this.headquartersService = headquartersService;
    }

    @PostMapping
    public ResponseEntity<GenericMessage> createHeadquarters(
            @Valid @RequestBody HeadquartesDTO headquarters
            ){
        headquartersService.createHeadquarters(headquarters);
        return ResponseEntity.ok(new GenericMessage("Matriz de Treinamento Criado com Sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public Page<HeadquartesDTO> getAll(
            Pageable pageable
    ){
        return headquartersService.findHeadquarters(pageable);
    }

    @GetMapping("/{id}")
    public HeadquartesDTO getHeadquarters(
            @PathVariable String id
    ){
        return headquartersService.findHeadquartersById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericMessage> updateHeadquarters(
            @PathVariable String id,
            @Valid @RequestBody HeadquartesDTO headquarters
    ){
        headquartersService.updateHeadquarters(id, headquarters);
        return ResponseEntity.ok(new GenericMessage("Matriz de Treinamento Editado com Sucesso", HttpStatus.OK.value()));

    }
}
