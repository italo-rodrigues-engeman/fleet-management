package com.indux.modules.contracts.presentation.controller;

import com.indux.modules.contracts.application.dto.ContractDTO;
import com.indux.modules.contracts.application.dto.ContractRegionalResponseDTO;
import com.indux.modules.contracts.application.service.ContractRegionalMapperService;
import com.indux.modules.contracts.application.service.GetContractByIdUseCase;
import com.indux.modules.contracts.application.service.ListContractsByRegionalUseCase;
import com.indux.modules.contracts.application.service.ListContractsUseCase;
import com.indux.modules.contracts.domain.model.Contract;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ListContractsUseCase listContractsUseCase;
    private final GetContractByIdUseCase getContractByIdUseCase;
    private final ListContractsByRegionalUseCase listContractsByRegionalUseCase;
    private final ContractRegionalMapperService contractRegionalMapperService;
    private final com.indux.core.domain.repository.generic.ContractProjectRepository contractProjectRepository;

    @GetMapping
    public ResponseEntity<Page<Contract>> list(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String nomeProjeto,
            @RequestParam(required = false) String regional,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) String codSap,
            @RequestParam(required = false) String gestorInterno,
            @RequestParam(required = false) String gestorCliente,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(listContractsUseCase.execute(
                cliente,
                nomeProjeto,
                regional,
                ativo,
                os,
                codSap,
                gestorInterno,
                gestorCliente,
                pageable
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(getContractByIdUseCase.execute(id));
    }

    @GetMapping("/regional/{regionalId}")
    public ResponseEntity<Page<Contract>> getByRegional(
            @PathVariable Long regionalId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(listContractsByRegionalUseCase.execute(regionalId, pageable));
    }

    @GetMapping("/regional")
    public ResponseEntity<Page<ContractRegionalResponseDTO>> getByRegionals(
            @RequestParam("ids") java.util.List<Long> regionalIds,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Contract> contracts = listContractsByRegionalUseCase.execute(regionalIds, pageable);
        Page<ContractRegionalResponseDTO> responseDTOs = contracts.map(contractRegionalMapperService::mapToResponseDTO);
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/regional/by-name/{regionalName}")
    public ResponseEntity<Page<Contract>> getByRegionalName(
            @PathVariable String regionalName,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(listContractsByRegionalUseCase.execute(regionalName, pageable));
    }

    // Novo endpoint: contratos ativos com rateio válido (não nulo) e filtro por nome do projeto
    @GetMapping("/rateio")
    public ResponseEntity<java.util.List<SimpleActiveContractDTO>> listActiveWithRateio(
            @RequestParam(required = false, name = "nome") String nomeFilter,
            @RequestParam(required = false, name = "ativo") Boolean ativo
    ) {
        // Buscar todos (ativos e inativos) e aplicar filtro de ativo se informado
        var todos = contractProjectRepository.findAllWithFilial();
        var filtered = todos.stream()
                .filter(c -> c.getRateio() != null)
                .filter(c -> ativo == null || (c.getAtivo() != null && c.getAtivo().equals(ativo)))
                .filter(c -> {
                    if (nomeFilter == null || nomeFilter.isBlank()) return true;
                    String f = nomeFilter.toLowerCase();
                    String projeto = c.getProjectName() != null ? c.getProjectName().toLowerCase() : "";
                    String centro = c.getCostCenterName() != null ? c.getCostCenterName().toLowerCase() : "";
                    return projeto.contains(f) || centro.contains(f);
                })
                .map(c -> new SimpleActiveContractDTO(c.getProjectName(), c.getRateio(), c.getCostCenterName()))
                .sorted(java.util.Comparator.comparing(SimpleActiveContractDTO::rateio))
                .toList();
        return ResponseEntity.ok(filtered);
    }

    public record SimpleActiveContractDTO(String nome_projeto, Integer rateio, String nome_centro_custos) {}
} 