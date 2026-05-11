package com.indux.modules.purchase_occurrence.presentation;

import com.indux.modules.purchase_occurrence.application.PurchaseOccurrenceCausesService;
import com.indux.modules.purchase_occurrence.domain.entities.PurchaseOccurrenceCauses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes/purchase-causes")
@RequiredArgsConstructor
public class PurchaseOccurrenceCausesController {

    private final PurchaseOccurrenceCausesService service;

    @GetMapping("/all")
    public ResponseEntity<List<PurchaseOccurrenceCauses>> getAllCauses() {
        return ResponseEntity.ok(service.fetchAll());
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createCause(@RequestBody PurchaseOccurrenceCauses cause) {
        service.create(cause);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCause(@PathVariable Long id) {
        service.deleteComplaintType(id);
        return ResponseEntity.ok().build();
    }
}