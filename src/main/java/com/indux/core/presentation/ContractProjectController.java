package com.indux.core.presentation;

import com.indux.core.domain.model.employee.ContractProject;
import com.indux.core.domain.repository.generic.ContractProjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contrato")
public class ContractProjectController {
    private final ContractProjectRepository contractRepository;

    public ContractProjectController(ContractProjectRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllContract(
            @RequestParam(name = "filiais", required = false) List<Integer> filiais,
            @RequestParam(name = "apenasAtivos", required = false, defaultValue = "false") Boolean apenasAtivos,
            @RequestParam(name = "nome", required = false) String nome
    ) {
        List<ContractProject> projects;

        if (filiais != null && !filiais.isEmpty()) {
            if (apenasAtivos) {
                projects = contractRepository.findAllByFilialCodigoFilialInAndAtivoTrue(filiais);
            } else {
            projects = contractRepository.findAllByFilialCodigoFilialIn(filiais);
            }
        } else {
            if (apenasAtivos) {
                projects = contractRepository.findAllWithFilialAndAtivoTrue();
        } else {
            projects = contractRepository.findAllWithCoordinators();
            }
        }
        
        // Aplicar filtro por nome se fornecido
        if (nome != null && !nome.isBlank()) {
            projects = projects.stream()
                .filter(c -> c.getCostCenterName() != null && 
                           c.getCostCenterName().toLowerCase().contains(nome.toLowerCase()))
                .toList();
        }
        
        List<Map<String, Object>> dtos = projects.stream()
                .map(ContractProject::toDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/resumo")
    public ResponseEntity<List<Map<String, Object>>> getResumoContratos(
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "apenasAtivos", required = false, defaultValue = "false") Boolean apenasAtivos
    ) {
        boolean ativoFilter = Boolean.TRUE.equals(apenasAtivos);
        String nomeFilter = (nome != null && !nome.isBlank()) ? nome : null;

        List<ContractProject> projects = contractRepository.findResumoContratos(nomeFilter, ativoFilter);

        List<Map<String, Object>> dtos = projects.stream()
            .map(c -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", c.getId());
                map.put("rateio", c.getRateio());
                map.put("projectName", c.getProjectName());
                return map;
            })
            .toList();
        return ResponseEntity.ok(dtos);
    }
}
