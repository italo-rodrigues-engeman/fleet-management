package com.indux.modules.budgets.presentation;

import com.indux.modules.budgets.application.dto.SimpleBudgetResponseDTO;
import com.indux.modules.budgets.application.dto.SimpleBudgetSummaryDTO;
import com.indux.modules.budgets.application.dto.CreateSimpleBudgetRequest;
import com.indux.modules.budgets.application.dto.CreateSimpleBudgetResponseDTO;
import com.indux.modules.budgets.application.dto.SimpleBudgetFilter;
import com.indux.modules.budgets.application.dto.UpdateBudgetItemsRequest;
import com.indux.modules.budgets.application.dto.AddBudgetServicesRequest;
import com.indux.modules.budgets.application.dto.UpdateServiceSupplierValuesRequest;
import com.indux.modules.budgets.application.dto.ApproveBudgetRequest;
import com.indux.modules.budgets.application.dto.CreateItemGroupRequest;
import com.indux.modules.budgets.application.dto.SimpleBudgetItemGroupResponseDTO;
import com.indux.modules.budgets.application.service.SimpleBudgetService;
import com.indux.modules.budgets.application.service.SimpleBudgetExcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simple-budgets")
@RequiredArgsConstructor
public class SimpleBudgetController {

    private final SimpleBudgetService simpleBudgetService;
    private final SimpleBudgetExcelService excelService;

    @PostMapping
    public ResponseEntity<CreateSimpleBudgetResponseDTO> createBudget(
            @Valid @RequestBody CreateSimpleBudgetRequest request,
            JwtAuthenticationToken jwt) {
        
        CreateSimpleBudgetResponseDTO response = simpleBudgetService.createBudget(request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleBudgetResponseDTO> getBudgetById(@PathVariable String id) {
        SimpleBudgetResponseDTO response = simpleBudgetService.getBudgetById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SimpleBudgetSummaryDTO>> getAllBudgets() {
        List<SimpleBudgetSummaryDTO> budgets = simpleBudgetService.getAllBudgets();
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/pesquisar")
    public ResponseEntity<Page<SimpleBudgetSummaryDTO>> searchBudgets(
            @ModelAttribute SimpleBudgetFilter filter,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SimpleBudgetSummaryDTO> response = simpleBudgetService.searchBudgets(filter, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/itens")
    public ResponseEntity<SimpleBudgetResponseDTO> updateItems(
            @PathVariable String id,
            @RequestBody UpdateBudgetItemsRequest request,
            JwtAuthenticationToken jwt) {
        SimpleBudgetResponseDTO response = simpleBudgetService.updateItems(id, request, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/itens/valores")
    public ResponseEntity<SimpleBudgetResponseDTO> updateItemValues(
            @PathVariable String id,
            @RequestBody UpdateBudgetItemsRequest request,
            JwtAuthenticationToken jwt) {
        SimpleBudgetResponseDTO response = simpleBudgetService.updateItemValues(id, request, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/servicos")
    public ResponseEntity<SimpleBudgetResponseDTO> addServices(
            @PathVariable String id,
            @ModelAttribute AddBudgetServicesRequest request,
            JwtAuthenticationToken jwt) {
        SimpleBudgetResponseDTO response = simpleBudgetService.addServices(id, request, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/servicos/valores")
    public ResponseEntity<SimpleBudgetResponseDTO> updateServiceValues(
            @PathVariable String id,
            @RequestBody UpdateServiceSupplierValuesRequest request,
            JwtAuthenticationToken jwt) {
        SimpleBudgetResponseDTO response = simpleBudgetService.updateServiceSupplierValues(id, request, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<SimpleBudgetResponseDTO> approveBudget(
            @PathVariable String id,
            @RequestBody ApproveBudgetRequest request,
            JwtAuthenticationToken jwt) {
        SimpleBudgetResponseDTO response = simpleBudgetService.approveBudget(id, request, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/itens/{codMega}")
    public ResponseEntity<SimpleBudgetResponseDTO> deleteItem(
            @PathVariable String id,
            @PathVariable Integer codMega,
            JwtAuthenticationToken jwt) {
        SimpleBudgetResponseDTO response = simpleBudgetService.deleteItem(id, codMega, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/servicos/{serviceName}/fornecedores/{supplierName}/desselecionar")
    public ResponseEntity<SimpleBudgetResponseDTO> unselectServiceSupplier(
            @PathVariable String id,
            @PathVariable String serviceName,
            @PathVariable String supplierName,
            JwtAuthenticationToken jwt) {
        SimpleBudgetResponseDTO response = simpleBudgetService.unselectServiceSupplier(id, serviceName, supplierName, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/groups")
    public ResponseEntity<SimpleBudgetItemGroupResponseDTO> createItemGroup(
            @Valid @RequestBody CreateItemGroupRequest request) {
        SimpleBudgetItemGroupResponseDTO response = simpleBudgetService.createItemGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<SimpleBudgetItemGroupResponseDTO>> getAllItemGroups() {
        List<SimpleBudgetItemGroupResponseDTO> response = simpleBudgetService.getAllItemGroups();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteItemGroup(@PathVariable String id) {
        simpleBudgetService.deleteItemGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download-excel")
    public ResponseEntity<byte[]> downloadBudgetExcel(@PathVariable String id) {
        byte[] excelBytes = excelService.generateBudgetExcel(id);
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=orcamento_" + id + ".xlsx")
                .body(excelBytes);
    }
}

