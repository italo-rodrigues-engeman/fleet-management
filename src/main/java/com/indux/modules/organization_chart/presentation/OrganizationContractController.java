package com.indux.modules.organization_chart.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.CreateContractDTO;
import com.indux.modules.organization_chart.application.services.ContractService;
import com.indux.modules.organization_chart.domain.entities.models.ContractType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization/contract")
public class OrganizationContractController {
    private final ContractService service;

    public OrganizationContractController(ContractService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<GenericMessage> createContract(
            @RequestBody @Validated CreateContractDTO dto) {
        service.createContract(dto);
        return ResponseEntity.ok(new GenericMessage("Contrato criado com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<ContractDTO>> getAllContracts(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "hierarquiaId", required = false) List<Long> organizationId,
            @RequestParam(value = "tipo", required = false) ContractType tipo,
            Pageable pageable) {
        return ResponseEntity.ok(service.searchContracts(pageable, search, organizationId, tipo));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GenericMessage> updateContract(
            @PathVariable Long id,
            @RequestBody @Validated CreateContractDTO dto) {
        service.updateContract(id, dto);
        return ResponseEntity.ok(new GenericMessage("Contrato editado com sucesso", HttpStatus.OK.value()));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<GenericMessage> deleteContract(
            @PathVariable Long id) {
        service.deleteContract(id);
        return ResponseEntity.ok(new GenericMessage("Contrato deletado com sucesso", HttpStatus.OK.value()));
    }
}
