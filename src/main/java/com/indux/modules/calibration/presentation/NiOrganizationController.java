package com.indux.modules.calibration.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.calibration.aplication.dtos.NiOrganizationCreate;
import com.indux.modules.calibration.aplication.dtos.NiOrganizationReturn;
import com.indux.modules.calibration.aplication.service.NiOrganizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gc/ni-organization")
public class NiOrganizationController {

    private final NiOrganizationService niOrganizationService;

    public NiOrganizationController(NiOrganizationService niOrganizationService) {
        this.niOrganizationService = niOrganizationService;
    }

    @PostMapping
    public ResponseEntity<GenericMessage> createNiOrganization(
            @RequestBody NiOrganizationCreate niOrganization) {
        niOrganizationService.createNiOrganization(niOrganization);
        return ResponseEntity.ok(new GenericMessage("Mega relacionando com o organograma", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<Page<NiOrganizationReturn>> getAllNiOrganizations(
            Pageable pageable,
            @RequestParam(required = false) String propriedadeId,
            @RequestParam(required = false) String search
    ){
        return ResponseEntity.ok(niOrganizationService.getAllNiOrganizations(pageable, propriedadeId, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NiOrganizationReturn> getNiOrganization(
            @PathVariable String id
    ){
        return ResponseEntity.ok(niOrganizationService.getHeritageById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<GenericMessage> updateNiOrganization(
            @PathVariable String id,
            @RequestBody NiOrganizationCreate niOrganization) {
        niOrganizationService.updateNiOrganization(id, niOrganization);
        return ResponseEntity.ok(new GenericMessage("Relacionamento atualizado com sucesso", HttpStatus.OK.value()));
    }
}