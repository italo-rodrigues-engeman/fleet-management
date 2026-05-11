package com.indux.modules.calibration.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.calibration.aplication.dtos.Properties;
import com.indux.modules.calibration.aplication.service.PropertiesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gc/properties")
public class PropertiesController {
    private final PropertiesService propertiesService;

    public PropertiesController(PropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    @PostMapping
    public ResponseEntity<GenericMessage> createProperties(
            @RequestBody Properties properties
            ){
        propertiesService.createProperties(properties);
        return ResponseEntity.ok(new GenericMessage("Propriedade criada com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<Page<Properties>> getProperties(
            @RequestParam(required = false) Boolean status,
            @RequestParam(value = "search", required = false) Object search,
            Pageable  pageable
    ){
        if (search != null) {
            return ResponseEntity.ok(propertiesService.searchProperties(pageable, search, status));
        }
        return ResponseEntity.ok(propertiesService.getAllProperties(status,pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericMessage> updateProperties(
            @PathVariable String id,
            @RequestBody Properties properties
    ){
        propertiesService.editProperties(id, properties);
        return ResponseEntity.ok(new GenericMessage("Propriedade atualizada com sucesso", HttpStatus.OK.value()));
    }
}