package com.indux.modules.crm.presentation;

import com.indux.modules.crm.application.dto.filter.EngemanAgentFilter;
import com.indux.modules.crm.application.dto.request.EngemanAgentRequest;
import com.indux.modules.crm.application.dto.response.EngemanAgentResponse;
import com.indux.modules.crm.application.service.EngemanAgentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/crm/engemanAgent")
public class EngemanAgentController {

    private final EngemanAgentService service;

    public EngemanAgentController(EngemanAgentService service) {
        this.service = service;
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EngemanAgentResponse> create(
            @RequestPart("dados") EngemanAgentRequest request,
            @RequestPart(value = "anexos", required = false) List<MultipartFile> anexos) {

        EngemanAgentResponse response = service.create(request, anexos);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EngemanAgentResponse> update(
            @PathVariable String id,
            @RequestPart("dados") EngemanAgentRequest req,
            @RequestPart(value = "anexos", required = false) List<MultipartFile> anexos) {

        EngemanAgentResponse response = service.update(req, id, anexos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EngemanAgentResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/")
    public ResponseEntity<Page<EngemanAgentResponse>> getAll(
            @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<EngemanAgentResponse>> filter(
            EngemanAgentFilter filter,
            @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.filter(filter, pageable));
    }

    @PatchMapping("/{id}")
    public void toggleStatus(@PathVariable String id) {
        service.toggleStatus(id);
    }
}