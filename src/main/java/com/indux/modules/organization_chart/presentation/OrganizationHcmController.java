package com.indux.modules.organization_chart.presentation;

import com.indux.modules.organization_chart.application.services.HcmService;
import com.indux.modules.organization_chart.domain.entities.jpa.HcmEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization/hcm")
public class OrganizationHcmController {
    private final HcmService hcmService;

    public OrganizationHcmController(HcmService hcmService) {
        this.hcmService = hcmService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<HcmEntity>> getAllHcm(
            Pageable pageable) {
        return ResponseEntity.ok(hcmService.getAllHcm(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<HcmEntity>> filterFilial(
            Pageable pageable,
            @RequestParam Object valor) {
        return ResponseEntity.ok(hcmService.search(valor, pageable));
    }

    @GetMapping("/getActive")
    public ResponseEntity<Page<HcmEntity>> getActiveHcm(
            Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(hcmService.getHcmFromActiveProjects(search, pageable));
    }
}
