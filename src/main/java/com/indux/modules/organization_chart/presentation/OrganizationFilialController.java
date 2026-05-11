package com.indux.modules.organization_chart.presentation;

import com.indux.modules.organization_chart.application.services.FilialService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organization/filial")
public class OrganizationFilialController {

    private final FilialService  filialService;

    public OrganizationFilialController(FilialService filialService) {
        this.filialService = filialService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<FilialHcmEntity>> getAllFilials(
            Pageable pageable) {
        return ResponseEntity.ok(filialService.getAllFilialHcm(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<FilialHcmEntity>> filterFilial(
            Pageable pageable,
            @RequestParam Object valor
    ) {
        return ResponseEntity.ok(filialService.search(valor, pageable));
    }

    @GetMapping("/getOrganization")
    public ResponseEntity<Page<Map<String, Object>>> getOrganization(
            @RequestParam(required = false) List<Integer> filialHCM,
            @RequestParam(required = false) String search,
            Pageable pageable
    ){
        List<Map<String, Object>> filiais = filialService.filialOrganizationSearch(filialHCM,search);
        int total = filiais.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);

        List<Map<String, Object>> pageContent = (start >= total)
                ? Collections.emptyList()
                : filiais.subList(start, end);
        return ResponseEntity.ok(new PageImpl<>(pageContent, pageable, total));
    }

    @GetMapping("/notAssociated")
    public ResponseEntity<Page<FilialHcmEntity>> getNotAssociated(
            Pageable pageable
    ){
        return ResponseEntity.ok(filialService.findNotAssociated(pageable));
    }

}
