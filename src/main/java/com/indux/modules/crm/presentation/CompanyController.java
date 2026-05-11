package com.indux.modules.crm.presentation;

import com.indux.modules.crm.application.dto.filter.CompanyFilter;
import com.indux.modules.crm.application.dto.filter.LeadFilter;
import com.indux.modules.crm.application.dto.filter.UnitFilter;
import com.indux.modules.crm.application.dto.filter.UnitFilterAll;
import com.indux.modules.crm.application.dto.request.CompanyRequest;
import com.indux.modules.crm.application.dto.request.LeadRequest;
import com.indux.modules.crm.application.dto.request.UnitRequest;
import com.indux.modules.crm.application.dto.response.*;
import com.indux.modules.crm.application.service.CompanyService;
import com.indux.modules.crm.application.service.LeadService;
import com.indux.modules.crm.application.service.UnitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/crm/company")
public class CompanyController {

    private final CompanyService service;
    private final UnitService unitService;
    private final LeadService leadService;

    public CompanyController(CompanyService service, UnitService unitService, LeadService leadService) {
        this.service = service;
        this.unitService = unitService;
        this.leadService = leadService;
    }

    @PostMapping("/")
    public ResponseEntity<CompanyResponse> create(
            @RequestBody CompanyRequest request,
            JwtAuthenticationToken jwt
            ){
        var user = UUID.fromString(jwt.getName());
        var result = service.create(request, user);
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(result);
    }

    @GetMapping("/")
    public ResponseEntity<Page<CompanySummary>> getAll(
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable
    ) {
        Page<CompanySummary> result = service.getAll(pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getById(
            @PathVariable String id,
            UUID moduleId,
            JwtAuthenticationToken jwt
    ) {
        var user = UUID.fromString(jwt.getName());
        var result = service.getById(id, moduleId, user);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> update(
            @RequestBody CompanyRequest request,
            @PathVariable String id
    ) {
        CompanyResponse result = service.update(request, id);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    public void toggleStatus(
            @PathVariable String id
    ) {
        service.toggleStatus(id);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable String id
    ) {
        service.delete(id);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<CompanySummary>> filter(
            CompanyFilter filter,
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable
    ) {
        Page<CompanySummary> result = service.filter(filter, pageable);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/unit/{companyId}")
    public ResponseEntity<UnitResponse> createUnit(
            @PathVariable String companyId,
            @RequestBody UnitRequest request,
            JwtAuthenticationToken jwt
    )
    {
        var user = UUID.fromString(jwt.getName());
        var result = unitService.create(request, companyId, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/units")
    public ResponseEntity<Page<UnitResponse>> getAllUnits(
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable
    ) {
     Page<UnitResponse> response = unitService.getAll(pageable);

     return ResponseEntity.ok(response);
    }

    @GetMapping("/unit/{id}")
    public ResponseEntity<UnitResponse> getUnit(
        @PathVariable String id
    ) {
        var result = unitService.getById(id);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/units/{companyId}")
    public ResponseEntity<Page<UnitResponse>> getUnitsByCompany(
            @PathVariable String companyId,
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable
    ) {
        Page<UnitResponse> result = unitService.getAllByCompany(pageable, companyId);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/unit/{id}")
    public ResponseEntity<UnitResponse> updateUnit(
            @PathVariable String id,
            @RequestBody UnitRequest request
    ) {
        UnitResponse result = unitService.update(request, id);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/unit/{companyId}/filter")
    public ResponseEntity<Page<UnitResponse>> filterUnit(
            @PathVariable String companyId,
            UnitFilter filter,
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable
    ) {
        Page<UnitResponse> result = unitService.filter(filter, companyId, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/units/filter")
    public ResponseEntity<Page<UnitResponse>> filterAllUnits(
            UnitFilterAll filter,
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable
    ) {
        Page<UnitResponse> result = unitService.filterAll(filter, pageable);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/unit/{id}")
    public void deleteUnit(
            @PathVariable String id
    ) {
        unitService.delete(id);
    }

    @PatchMapping("/unit/{id}")
    public void toggleUnitStatus(
            @PathVariable String id
    ) {
        unitService.toggleStatus(id);
    }

    @PostMapping("/lead/{unitId}")
    public ResponseEntity<LeadResponse> createLead(
            @PathVariable String unitId,
            @RequestBody LeadRequest request,
            JwtAuthenticationToken jwt
            ) {
        var user = UUID.fromString(jwt.getName());

        var result = leadService.create(request, unitId, user);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/leads")
    public ResponseEntity<Page<LeadResponse>> getAllLeads(
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable
    ) {
        Page<LeadResponse> response = leadService.getAll(pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/leads/{companyId}")
    public ResponseEntity<Page<LeadResponse>> getAllLeads(
            @PathVariable String companyId,
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable
    ) {
        var result = leadService.getAllByCompany(pageable, companyId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/lead/{id}")
    public ResponseEntity<LeadResponse> getLeadById(
            @PathVariable String id
    ) {
        LeadResponse response = leadService.getById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/lead/{id}")
    public ResponseEntity<LeadResponse> updateLead(
            @PathVariable String id,
            @RequestBody LeadRequest request
    ) {
        LeadResponse result = leadService.update(request, id);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/lead/{id}")
    public void toggleLeadStatus(
            @PathVariable String id
    ) {
        leadService.toggleStatus(id);
    }

    @DeleteMapping("/lead/{id}")
    public void deleteLead(
            @PathVariable String id
    ) {
        leadService.delete(id);
    }

    @GetMapping("/lead/filter")
    public ResponseEntity<Page<LeadResponse>> filterLeads(
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable,
            LeadFilter filter
    ) {
        Page<LeadResponse> leads = leadService.filter(pageable, filter);

        return ResponseEntity.ok(leads);
    }
}
