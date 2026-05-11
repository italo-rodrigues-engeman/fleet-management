package com.indux.core.presentation;

import com.indux.core.application.service.generic.BranchService;
import com.indux.core.domain.model.employee.Filial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filiais")
public class BranchController {
    private final BranchService service;

    public BranchController(BranchService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Filial>> getFiliais() {
        return ResponseEntity.ok(service.getAllFiliais());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Filial>> search(
            @RequestParam(value = "search", required = false) String search,
            @PageableDefault(size = 10, sort = "branchId", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(service.searchBranches(search, pageable));
    }
}
