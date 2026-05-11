package com.indux.core.presentation;

import com.indux.core.application.service.employee.PositionService;
import com.indux.core.domain.model.employee.EmployeePosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/position")
public class PositionController {
    private final PositionService service;

    public PositionController(PositionService service) {
        this.service = service;
    }

    @GetMapping("/all/distinct")
    public ResponseEntity<Page<EmployeePosition>> fetchAllDistinct(
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.fetchAllWithoutRepetition(pageable));
    };

    @GetMapping("/all")
    public ResponseEntity<Page<EmployeePosition>> fetchAll(
            Pageable pageable
    ){
        return ResponseEntity.ok(service.fetchAll(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<EmployeePosition>> filter(
            Pageable pageable,
            @RequestParam String nome
    ) {
        return ResponseEntity.ok((service.filterByName(nome, pageable)));
    }
}
