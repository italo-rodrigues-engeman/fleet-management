package com.indux.modules.organization_chart.presentation;

import com.indux.modules.organization_chart.application.services.MegaService;
import com.indux.modules.organization_chart.domain.entities.jpa.MegaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organization/mega")
public class OrganizationMegaController {

    private final MegaService megaService;

    public OrganizationMegaController(MegaService megaService) {
        this.megaService = megaService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<MegaEntity>> getAllProjects(
            Pageable pageable
    ) {
        return ResponseEntity.ok(megaService.getAllMegas(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<MegaEntity>> filterFilial(
            Pageable pageable,
            @RequestParam Object valor
    ) {
        return ResponseEntity.ok(megaService.search(valor, pageable));
    }

    @GetMapping("/notAssociated")
    public ResponseEntity<Page<MegaEntity>> notAssociated(
            Pageable pageable
    ){
        return ResponseEntity.ok(megaService.notAssociated(pageable));
    }
}
