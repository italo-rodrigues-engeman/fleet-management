package com.indux.modules.crm.presentation;

import com.indux.modules.crm.application.dto.filter.CommercialInteractionsFilter;
import com.indux.modules.crm.application.dto.request.CommercialInteractionsRequest;
import com.indux.modules.crm.application.dto.request.EngemanAgentRequest;
import com.indux.modules.crm.application.dto.response.CommercialInteractionsResponse;
import com.indux.modules.crm.application.service.CommercialInteractionsService;
import com.indux.modules.crm.domain.entity.CommercialInteractions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/crm/commercialInteractions")
public class CommercialInteractionsController {

    private final CommercialInteractionsService service;

    public CommercialInteractionsController(CommercialInteractionsService service) {
        this.service = service;
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommercialInteractionsResponse> create(
            @RequestPart("dados") CommercialInteractionsRequest request,
            @RequestPart(value = "arquivo_atencao", required = false) List<MultipartFile> arquivosAtencao,
            @RequestPart(value = "arquivo_descricao", required = false) List<MultipartFile> arquivosDescricao,
            @RequestPart(value = "arquivo_oportunidade", required = false) List<MultipartFile> arquivosOportunidade
    ) {

        CommercialInteractionsResponse response = service.create(
                request,
                arquivosAtencao,
                arquivosDescricao,
                arquivosOportunidade
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<Page<CommercialInteractionsResponse>> getAll(
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable
    ) {
        Page<CommercialInteractionsResponse> response = service.getAll(pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<Page<CommercialInteractionsResponse>> getAllByCompany(
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable,
            @PathVariable String companyId
    ) {
        Page<CommercialInteractionsResponse> responses = service.getAllByCompany(pageable, companyId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommercialInteractionsResponse> getById(
            @PathVariable String id
    ) {
        CommercialInteractionsResponse response = service.getById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<CommercialInteractionsResponse>> filter(
            CommercialInteractionsFilter filter,
            @PageableDefault(size = 50, page = 0, sort = "id") Pageable pageable
    ) {
        Page<CommercialInteractionsResponse> response = service.filter(filter, pageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommercialInteractionsResponse> update(
            @PathVariable String id,
            @RequestPart("dados") CommercialInteractionsRequest request,
            @RequestPart(value = "arquivo_atencao", required = false) List<MultipartFile> arquivosAtencao,
            @RequestPart(value = "arquivo_descricao", required = false) List<MultipartFile> arquivosDescricao,
            @RequestPart(value = "arquivo_oportunidade", required = false) List<MultipartFile> arquivosOportunidade
    ) {

        CommercialInteractionsResponse response = service.update(
                request,
                id,
                arquivosAtencao,
                arquivosDescricao,
                arquivosOportunidade
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public void toggleStatus(
            @PathVariable String id
    ) {
        service.toggleStatus(id);
    }
}
