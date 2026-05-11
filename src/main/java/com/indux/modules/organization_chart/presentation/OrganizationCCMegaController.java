package com.indux.modules.organization_chart.presentation;

import com.indux.modules.organization_chart.application.services.CCMegaService;
import com.indux.modules.organization_chart.domain.entities.jpa.CCMegaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organization/ccmega")
public class OrganizationCCMegaController {

    private final CCMegaService megaService;

    public OrganizationCCMegaController(CCMegaService megaService) {
        this.megaService = megaService;
    }


    @GetMapping("/getAll")
    public ResponseEntity<Page<CCMegaEntity>> getAllProjects(
            Pageable pageable
    ) {
        return ResponseEntity.ok(megaService.getAllMegas(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<CCMegaEntity>> filterFilial(
            Pageable pageable,
            @RequestParam Object valor
    ) {
        return ResponseEntity.ok(megaService.search(valor, pageable));
    }
}
