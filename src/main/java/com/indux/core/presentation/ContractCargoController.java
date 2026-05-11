package com.indux.core.presentation;

import com.indux.core.application.dto.generic.IdNameDTO;
import com.indux.core.domain.repository.generic.CargoRepository;
import com.indux.core.domain.repository.generic.ContractProjectRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee")
public class ContractCargoController {

    private final ContractProjectRepository contractRepo;
    private final CargoRepository cargoRepo;

    public ContractCargoController(ContractProjectRepository contractRepo, CargoRepository cargoRepo) {
        this.contractRepo = contractRepo;
        this.cargoRepo = cargoRepo;
    }

    @GetMapping("/contracts")
    public List<IdNameDTO> searchContracts(
            @RequestParam String nome
           
    ) {
        String term = nome.toLowerCase();
        return contractRepo.findAll().stream()
                .filter(c -> c.getRateio() != null && c.getCostCenterName() != null && !c.getCostCenterName().isBlank())
                .filter(c -> {
                    String costLower = c.getCostCenterName().toLowerCase();
                    String projLower = c.getProjectName() != null ? c.getProjectName().toLowerCase() : "";
                    return costLower.contains(term) || projLower.contains(term);
                })
                .map(c -> new IdNameDTO(String.valueOf(c.getRateio()), c.getCostCenterName()))
                
                .collect(Collectors.toList());
    }

    @GetMapping("/cargos")
    public List<IdNameDTO> searchCargos(
            @RequestParam String nome
           
    ) {
        String term = nome.toLowerCase();
        return cargoRepo.findAll().stream()
                .filter(c -> c.getNameTitle() != null && c.getNameTitle().toLowerCase().contains(term))
                
                .map(c -> new IdNameDTO(c.getIdHcm(), c.getNameTitle()))
                .collect(Collectors.toList());
    }
} 