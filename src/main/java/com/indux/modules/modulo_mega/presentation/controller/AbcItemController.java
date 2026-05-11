package com.indux.modules.modulo_mega.presentation.controller;

import com.indux.modules.modulo_mega.application.dto.abc.AbcItemFilter;
import com.indux.modules.modulo_mega.application.dto.abc.AbcItemResponse;
import com.indux.modules.modulo_mega.service.PurchaseMegaAbcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/curva-abc/itens")
@RequiredArgsConstructor
public class AbcItemController {

    private final PurchaseMegaAbcService abcService;

    @PostMapping
    public ResponseEntity<List<AbcItemResponse>> calculateAbcItemCurve(@RequestBody AbcItemFilter filter) {
        return ResponseEntity.ok(abcService.calculateAbcCurve(filter));
    }
}
