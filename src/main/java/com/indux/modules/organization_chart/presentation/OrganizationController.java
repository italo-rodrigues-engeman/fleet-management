package com.indux.modules.organization_chart.presentation;

import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.organization_chart.application.dtos.*;
import com.indux.modules.organization_chart.application.services.OrganizationService;
import com.indux.modules.organization_chart.application.services.SubordinateService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationFilialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/organization/")
public class OrganizationController {
    private final OrganizationService service;
    private final SubordinateService subordinateService;
    private final OrganizationFilialRepository filialRepository;

    public OrganizationController(OrganizationService organizationService, SubordinateService subordinateService, OrganizationFilialRepository filialRepository) {
        this.service = organizationService;
        this.subordinateService = subordinateService;
        this.filialRepository = filialRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<GenericMessage> createOrganization(
            @RequestBody @Validated CreateOrganizationDTO dto
    ) {
        service.createOrganization(dto);
        return ResponseEntity.ok(new GenericMessage("Organograma criada com sucesso", HttpStatus.CREATED.value()));
    }


    @GetMapping("/getAll")
    public ResponseEntity<Page<OrganizationDTO>> getAllOrganizations(
             Pageable pageable
    ) {
        return ResponseEntity.ok(service.getAllOrganizations(pageable));
    }

    @GetMapping("getByType/{type}")
    public ResponseEntity<Page<OrganizationDTO>> getOrganizationsByType(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "subordinadoId", required = false) List<Long> subordinadoId,
            Pageable pageable,
            @PathVariable OrganizationType type
    ){
        return ResponseEntity.ok(service.getTypeOrganizations(pageable, type, search, subordinadoId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GenericMessage> updateOrganization(
            @PathVariable Long id,
            @RequestBody CreateOrganizationDTO dto
    ){
        service.updateOrganization(id, dto);
        return ResponseEntity.ok(new GenericMessage("Organograma editada com sucesso", HttpStatus.OK.value()));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<GenericMessage> deleteOrganization(
            @PathVariable Long id
    ){
        service.deleteOrganization(id);
        return ResponseEntity.ok(new GenericMessage("Organograma deletada com sucesso", HttpStatus.OK.value()));
    }

    @GetMapping("/subordinates/{directorId}")
    public ResponseEntity<AllSubordinatesResponseDTO> getAllSubordinatesByDirectorId(
            @PathVariable Long directorId
    ){
        return ResponseEntity.ok(subordinateService.getAllSubordinatesRecursivelyByDirectorId(directorId));
    }

    @GetMapping("/employees/{directorId}")
    public ResponseEntity<Page<EmployeeSummaryDTO>> getAllEmployeesByDirectorId(
            @PathVariable Long directorId,
            Pageable pageable
    ){
        return ResponseEntity.ok(subordinateService.getAllEmployeesByDirectorId(directorId, pageable));
    }

    @GetMapping("/subordinates/superintendencia/{superintendenciaId}")
    public ResponseEntity<AllSubordinatesResponseDTO> getAllSubordinatesBySuperintendenciaId(
            @PathVariable Long superintendenciaId
    ){
        return ResponseEntity.ok(subordinateService.getAllSubordinatesRecursivelyBySuperintendenciaId(superintendenciaId));
    }

    @GetMapping("/employees/superintendencia/{superintendenciaId}")
    public ResponseEntity<Page<EmployeeSummaryDTO>> getAllEmployeesBySuperintendenciaId(
            @PathVariable Long superintendenciaId,
            Pageable pageable
    ){
        return ResponseEntity.ok(subordinateService.getAllEmployeesBySuperintendenciaId(superintendenciaId, pageable));
    }

    @GetMapping("/subordinates/regional/{regionalId}")
    public ResponseEntity<AllSubordinatesResponseDTO> getAllSubordinatesByRegionalId(
            @PathVariable Long regionalId
    ){
        return ResponseEntity.ok(subordinateService.getAllSubordinatesRecursivelyByRegionalId(regionalId));
    }

    @GetMapping("/employees/regional/{regionalId}")
    public ResponseEntity<Page<EmployeeSummaryDTO>> getAllEmployeesByRegionalId(
            @PathVariable Long regionalId,
            Pageable pageable
    ){
        return ResponseEntity.ok(subordinateService.getAllEmployeesByRegionalId(regionalId, pageable));
    }

    @GetMapping("/subordinates/setor/{setorId}")
    public ResponseEntity<AllSubordinatesResponseDTO> getAllSubordinatesBySetorId(
            @PathVariable Long setorId
    ){
        return ResponseEntity.ok(subordinateService.getAllSubordinatesRecursivelyBySetorId(setorId));
    }

    @GetMapping("/employees/setor/{setorId}")
    public ResponseEntity<Page<EmployeeSummaryDTO>> getAllEmployeesBySetorId(
            @PathVariable Long setorId,
            Pageable pageable
    ){
        return ResponseEntity.ok(subordinateService.getAllEmployeesBySetorId(setorId, pageable));
    }

    @GetMapping("/subordinates/contrato/{contratoId}")
    public ResponseEntity<ContractWithProjectsDTO> getAllSubordinatesByContratoId(
            @PathVariable Long contratoId
    ){
        return ResponseEntity.ok(subordinateService.getContratoWithProjects(contratoId));
    }

    @GetMapping("/employees/contrato/{contratoId}")
    public ResponseEntity<Page<EmployeeSummaryDTO>> getAllEmployeesByContratoId(
            @PathVariable Long contratoId,
            Pageable pageable
    ){
        return ResponseEntity.ok(subordinateService.getAllEmployeesByContratoId(contratoId, pageable));
    }

    @GetMapping("/contrato/{contratoId}/projects/details")
    public ResponseEntity<ContractProjectsDetailsDTO> getContratoProjectsWithDetails(
            @PathVariable Long contratoId
    ){
        return ResponseEntity.ok(subordinateService.getContratoProjectsWithDetails(contratoId));
    }

    @GetMapping("/contracts/diretoria/{diretoriaId}")
    public ResponseEntity<List<ContractSummaryDTO>> getAllContractsByDiretoriaId(
            @PathVariable Long diretoriaId
    ){
        return ResponseEntity.ok(subordinateService.getAllContractsByDiretoriaId(diretoriaId));
    }

    @GetMapping("/contracts/superintendencia/{superintendenciaId}")
    public ResponseEntity<List<ContractSummaryDTO>> getAllContractsBySuperintendenciaId(
            @PathVariable Long superintendenciaId
    ){
        return ResponseEntity.ok(subordinateService.getAllContractsBySuperintendenciaId(superintendenciaId));
    }

    @GetMapping("/contracts/regional/{regionalId}")
    public ResponseEntity<List<ContractSummaryDTO>> getAllContractsByRegionalId(
            @PathVariable Long regionalId
    ){
        return ResponseEntity.ok(subordinateService.getAllContractsByRegionalId(regionalId));
    }

    @GetMapping("/contracts/setor/{setorId}")
    public ResponseEntity<List<ContractSummaryDTO>> getAllContractsBySetorId(
            @PathVariable Long setorId
    ){
        return ResponseEntity.ok(subordinateService.getAllContractsBySetorId(setorId));
    }

    @GetMapping("/employees/filter")
    
    public ResponseEntity<Page<EmployeeSummaryDTO>> getEmployeesByFilters(
            @RequestParam(required = false) List<Long> diretoriaId,
            @RequestParam(required = false) List<Long> superintendenciaId,
            @RequestParam(required = false) List<Long> regionalId,
            @RequestParam(required = false) List<Long> setorId,
            @RequestParam(required = false) List<Long> contratoId,
            @RequestParam(required = false) List<Long> projetoId,
            @RequestParam(required = false) String centroCustoHcm,
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) List<String> nome,
            @RequestParam(required = false) List<String> matricula,
            @RequestParam(required = false) List<String> cidade,
            @RequestParam(required = false) List<String> estado,
            @RequestParam(required = false) List<String> sexo,
            @RequestParam(required = false) List<String> grauInstrucao,
            @RequestParam(required = false) List<String> filialId,
            @RequestParam(required = false) List<String> filialIdHcm,
            @RequestParam(required = false) List<String> cargo,
            Pageable pageable
    ){
        EmployeeFilterDTO filters = new EmployeeFilterDTO();
        filters.setDiretoriaId(diretoriaId);
        filters.setSuperintendenciaId(superintendenciaId);
        filters.setRegionalId(regionalId);
        filters.setSetorId(setorId);
        filters.setContratoId(contratoId);
        filters.setProjetoId(projetoId);
        filters.setCentroCustoHcm(centroCustoHcm);
        filters.setSituacao(situacao);
        filters.setNome(nome);
        filters.setMatricula(matricula);
        filters.setCidade(cidade);
        filters.setEstado(estado);
        filters.setSexo(sexo);
        filters.setGrauInstrucao(grauInstrucao);
        filters.setFilialId(filialId);
        filters.setFilialIdHcm(filialIdHcm);
        filters.setCargo(cargo);
        
        return ResponseEntity.ok(subordinateService.getEmployeesByFilters(filters, pageable));
    }

    /**
     * Endpoint to get HCM branches based on organizational filters.
     * Returns HCM branches that are in the intersection of the provided filters.
     * 
     * @param diretoriaId      Optional diretoria ID filter
     * @param superintendenciaId Optional superintendencia ID filter
     * @param regionalId       Optional regional ID filter
     * @param setorId          Optional setor ID filter
     * @param contratoId       Optional contract ID filter
     * @param projetoId        Optional project ID filter
     * @return List of HCM branches that match all provided filters
     */
    @GetMapping("/filiais-hcm")
    public ResponseEntity<Set<FilialHcmEntity>> getFiliaisHcmByFilters(
            @RequestParam(required = false) List<Long> diretoriaId,
            @RequestParam(required = false) List<Long> superintendenciaId,
            @RequestParam(required = false) List<Long> regionalId,
            @RequestParam(required = false) List<Long> setorId,
            @RequestParam(required = false) List<Long> contratoId,
            @RequestParam(required = false) List<Long> projetoId) {
        
        Set<FilialHcmEntity> filiaisHcm = subordinateService.getFiliaisHcmByFilters(
                diretoriaId, superintendenciaId, regionalId, setorId, contratoId, projetoId);
        
        return ResponseEntity.ok(filiaisHcm);
    }
}
