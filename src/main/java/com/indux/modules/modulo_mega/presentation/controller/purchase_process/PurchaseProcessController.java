package com.indux.modules.modulo_mega.presentation.controller.purchase_process;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.modulo_mega.application.dto.OrdersFilter;
import com.indux.modules.modulo_mega.application.mapper.PurchaseProcessApplicationMapper;
import com.indux.modules.modulo_mega.application.services.PurchaseProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mega/purchases")
@RequiredArgsConstructor
public class PurchaseProcessController {

    private final PurchaseProcessService purchaseProcessService;
    private final PurchaseProcessApplicationMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        try{
            final var entity = mapper.toResponse(purchaseProcessService.get(id));
            return ResponseEntity.ok(entity);
        } catch (ModuleNotFoundFailure e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getAll(Pageable pageable, OrdersFilter filter) {
            final var list = purchaseProcessService.getAll(pageable, filter);
            final var entities = mapper.toPurchaseProcessResponseList(list.getContent());
            return ResponseEntity.ok(new PageImpl<>(entities, pageable, list.getTotalElements()));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getAllStatus() {
        final var list = purchaseProcessService.getStatus();
        return ResponseEntity.ok(list);
    }
}
