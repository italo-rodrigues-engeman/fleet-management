package com.indux.core.presentation;

import com.indux.core.application.dto.cbo.*;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.service.cbo.CBODetailsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cbo")
public class CBOController {

    private final CBODetailsService cboDetailsService;

    public CBOController(CBODetailsService cboDetailsService) {
        this.cboDetailsService = cboDetailsService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<GetAllCBO>> getAll(
            @ModelAttribute FilterCBO filter,
            Pageable pageable) {
        return ResponseEntity.ok(cboDetailsService.getAll(filter, pageable));
    }

    @GetMapping("/getAllCBO")
    public ResponseEntity<Page<RelatedPosition>> getAllCBO(
            Pageable pageable) {
        return ResponseEntity.ok(cboDetailsService.findAllCBO(pageable));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<CBODTO> getById(
            @PathVariable String id) {
        return ResponseEntity.ok(cboDetailsService.findById(id));
    }

    @GetMapping("/getCod/{code}")
    public ResponseEntity<CBODTO> getByCodCBO(
            @PathVariable Integer code) {
        return ResponseEntity.ok(cboDetailsService.findByIdCBO(code));
    }

    @PostMapping("/matriz")
    public ResponseEntity<GenericMessage> postMatriz(@RequestBody MatrizCargoDTO request) {
        cboDetailsService.postMatriz(request.getFilialId(), request.getCargosId());
        return ResponseEntity.ok(new GenericMessage("Novos cargo registrados para a filial", HttpStatus.OK.value()));
    }

    @DeleteMapping("/matriz")
    public ResponseEntity<GenericMessage> deleteMatriz(@RequestBody MatrizCargoDTO request) {
        cboDetailsService.deleteMatriz(request.getFilialId(), request.getCargosId());
        return ResponseEntity.ok(new GenericMessage("Cargo removido da filial com sucesso", HttpStatus.OK.value()));
    }
}
