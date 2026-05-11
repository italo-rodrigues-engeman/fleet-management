package com.indux.modules.organization_chart.application.services;

import com.indux.core.domain.repository.generic.BranchRepository;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.organization_chart.application.dtos.CreateProjectDTO;
import com.indux.modules.organization_chart.application.dtos.CreateFilialDetail;
import com.indux.modules.organization_chart.application.dtos.ProjectFilter;
import com.indux.modules.organization_chart.domain.entities.jpa.*;
import com.indux.modules.organization_chart.domain.repositories.jpa.*;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationCCMegaRepository;
import com.indux.modules.organization_chart.infra.mapper.ProjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final OrganizationProjectRepository organizationProjectRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationContractRepository organizationContractRepository;
    private final OrganizationFilialRepository organizationFilialRepository;
    private final ProjectMapper projectMapper;
    private final BranchRepository branchRepository;
    private final SimpleContractRepository contractRepository;
    private final SimpleProjectRepository projectRepository;
    private final OrganizationCCMegaRepository ccMegaRepository;
    private final SubordinateService subordinateService;

    public ProjectService(OrganizationProjectRepository organizationProjectRepository,
            OrganizationRepository organizationRepository,
            OrganizationContractRepository organizationContractRepository,
            OrganizationFilialRepository organizationFilialRepository, ProjectMapper projectMapper,
            BranchRepository branchRepository, SimpleContractRepository contractRepository,
            SimpleProjectRepository projectRepository, OrganizationCCMegaRepository ccMegaRepository,
            SubordinateService subordinateService) {
        this.organizationProjectRepository = organizationProjectRepository;
        this.organizationRepository = organizationRepository;
        this.organizationContractRepository = organizationContractRepository;
        this.organizationFilialRepository = organizationFilialRepository;
        this.projectMapper = projectMapper;
        this.branchRepository = branchRepository;
        this.contractRepository = contractRepository;
        this.projectRepository = projectRepository;
        this.ccMegaRepository = ccMegaRepository;
        this.subordinateService = subordinateService;
    }

    public void createProject(CreateProjectDTO dto) {
//        validedCreate(dto);
        branchRepository.findById(dto.filialMega()).orElseThrow(() -> new ModuleNotFoundFailure("Filial não existe"));
        var project = projectMapper.toEntity(dto);
        List<FilialHcmEntity> filiais = new ArrayList<>();

        dto.filial().forEach(detail -> {
            FilialHcmEntity filialEntity = loadFilailHCMById(detail.filial());
            CCMegaEntity ccMega = loadCCMegaById(detail.ccMega());

            filialEntity.setStatus(detail.status());
            filialEntity.setCcMega(ccMega);

            filiais.add(filialEntity);
        });

        project.setFilial(filiais);
        if (dto.contrato() != null) {
            ContractEntity contract = organizationContractRepository.findByIdWithoutBranch(dto.contrato())
                    .orElseThrow(() -> new ModuleFailure("Contrato não encontrado."));
            project.setContract(contract);
        }

        if (dto.subordinado() != null) {
            OrganizationEntity subordinate = organizationRepository.findById(dto.subordinado())
                    .orElseThrow(() -> new ModuleFailure("Subordinado não encontrado."));
            project.setSubordinate(subordinate);
        }
        organizationProjectRepository.save(project);
    }

    private CCMegaEntity loadCCMegaById(Integer ccMegaId) {
        return ccMegaRepository.findById(ccMegaId)
                .orElseThrow(() -> new ModuleFailure("CC Mega não encontrado: " + ccMegaId));
    }

    private FilialHcmEntity loadFilailHCMById(Integer filialId) {
        return organizationFilialRepository.findById(filialId)
                .orElseThrow(() -> new ModuleFailure("CC Mega não encontrado: " + filialId));
    }

    @NotNull
    private List<FilialHcmEntity> getFilialHcmEntities(CreateProjectDTO dto) {
        List<Integer> filialIds = dto.filial().stream()
                .map(CreateFilialDetail::filial)
                .toList();

        List<FilialHcmEntity> result = organizationFilialRepository.findAllById(filialIds);

        if (result.size() != dto.filial().size()) {
            throw new ModuleFailure("Uma ou mais filiais não existem no banco.");
        }
        return result;
    }

    //Todo: REAVALIAR
//    private void validedCreate(CreateProjectDTO dto) {
//        validedRef(dto);
//        var mega = organizationProjectRepository.findByMegaCusInReduzido(dto.mega());
//        if (!mega.isEmpty())
//            throw new ModuleFailure("Esta mega já está vinculado.");
//        for (CreateFilialDetail detail : dto.filial()) {
//            if (organizationProjectRepository.countProjectsWithFilial(detail.filial()) > 0)
//                throw new ModuleFailure("A filial " + detail.filial() + " já está vinculada a outro projeto.");
//        }
//    }

    private static void validedRef(CreateProjectDTO dto) {
        if (dto.filial().isEmpty())
            throw new ModuleFailure("Precisa etá vinculado a uma filial.");
        if (dto.subordinado() == null && dto.contrato() == null) {
            throw new ModuleFailure("Referencie um contrato ou um subordinado.");
        }
        if (dto.subordinado() != null && dto.contrato() != null) {
            throw new ModuleFailure("Referencie um contrato ou um subordinado.");
        }
    }

    public Page<ProjectEntity> getAllProjects(
            ProjectFilter projectFilter,
            Pageable pageable) {
        if (projectFilter != null && (projectFilter.getRegional() != null && !projectFilter.getRegional().isEmpty())
            || (Objects.requireNonNull(projectFilter).getOrganization() != null )) {
            assert projectFilter.getRegional() != null;
            var filterRegional = projectFilter.getRegional() != null ? projectFilter.getRegional() : projectFilter.getOrganization();
            var filiaisEntities = subordinateService.getFiliaisHcmByFilters(projectFilter.getOrganization(), projectFilter.getOrganization(), filterRegional,
                    projectFilter.getOrganization(), null, null);
            List<Integer> filialIds = filiaisEntities.stream()
                    .map(FilialHcmEntity::getFilialId)
                    .collect(Collectors.toList());
            projectFilter.setFilial(filialIds);
            projectFilter.setRegional(null);
        }
        var result = organizationProjectRepository.getWithFilter(
                projectFilter,
                pageable);
        return result;
    }

    public void updateProject(Long id, CreateProjectDTO dto) {
        branchRepository.findById(dto.filialMega()).orElseThrow(() -> new ModuleNotFoundFailure("Filial não existe"));
        var newProject = projectMapper.toEntity(dto);
        var project = organizationProjectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Projeto não em contrado"));
        validedUpdate(dto, project);
        List<FilialHcmEntity> filiais = new ArrayList<>();

        dto.filial().forEach(detail -> {
            FilialHcmEntity filialEntity = loadFilailHCMById(detail.filial());
            CCMegaEntity ccMega = loadCCMegaById(detail.ccMega());

            filialEntity.setStatus(detail.status());
            filialEntity.setCcMega(ccMega);

            filiais.add(filialEntity);
        });

        newProject.setFilial(filiais);
        if (dto.contrato() != null) {
            ContractEntity contract = organizationContractRepository.findByIdWithoutBranch(dto.contrato())
                    .orElseThrow(() -> new ModuleFailure("Contrato não encontrado."));
            newProject.setContract(contract);
        }

        if (dto.subordinado() != null) {
            OrganizationEntity subordinate = organizationRepository.findById(dto.subordinado())
                    .orElseThrow(() -> new ModuleFailure("Subordinado não encontrado."));
            newProject.setSubordinate(subordinate);
        }

        newProject.setId(project.getId());
        organizationProjectRepository.save(newProject);
    }

    private void validedUpdate(CreateProjectDTO dto, ProjectEntity project) {
        validedRef(dto);
        if (!dto.mega().equals(project.getMega().getCusInReduzido())) {
            var mega = organizationProjectRepository.findByMegaCusInReduzido(dto.mega());
            if (!mega.isEmpty())
                throw new ModuleFailure("Esta mega já está vinculado.");
        }

        Set<Integer> currentFilialIds = project.getFilial().stream()
                .map(FilialHcmEntity::getFilialId)
                .collect(Collectors.toSet());

        for (CreateFilialDetail detail : dto.filial()) {
            if (!currentFilialIds.contains(detail.filial())) {
                //TODO: REAVALIAR
            }
//                if (organizationProjectRepository.countProjectsWithFilial(detail.filial()) > 0)
//                    throw new ModuleFailure("A filial " + detail.filial() + " já está vinculada a outro projeto.");
//            }
        }
    }

    public void deleteProject(Long id) {
        if (!organizationProjectRepository.existsById(id))
            throw new ModuleFailure("Projeto não encontrado");
        organizationProjectRepository.deleteById(id);
    }

    public ProjectEntity getbyId(Long id) {
        ProjectEntity project = organizationProjectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Projeto não encontrado"));

        // Garantir que o branch seja carregado (do projeto, não do contrato)
        if (project.getBranch() == null && project.getFilialMegaId() != null) {
            var filialOpt = branchRepository.findById(project.getFilialMegaId());
            filialOpt.ifPresent(project::setBranch);
        }

        // Garantir que o branch do contrato também seja carregado
        if (project.getContract() != null && project.getContract().getBranch() == null) {
            Long filialMegaId = project.getContract().getFilialMegaId();
            if (filialMegaId != null) {
                var filialOpt = branchRepository.findById(filialMegaId);
                filialOpt.ifPresent(filial -> project.getContract().setBranch(filial));
            }
        }

        return project;
    }

    public List<Long> getProjectIdsByOrganization(List<Long> organizationIds) {
        // Check if all organizations exist
        for (Long organizationId : organizationIds) {
            if (!organizationRepository.existsById(organizationId)) {
                throw new ModuleNotFoundFailure("Orgarnograma não encontrada.");
            }
        }

        List<OrganizationEntity> allSubordinates = new ArrayList<>();

        // Get all subordinates for each organization ID
        for (Long organizationId : organizationIds) {
            allSubordinates.addAll(findAllSubordinatesRecursively(organizationId));
        }

        // Get all subordinate IDs
        List<Long> subordinateIds = allSubordinates.stream()
                .map(OrganizationEntity::getId)
                .collect(Collectors.toList());

        // Get contracts related to subordinates
        List<SimpleContractEntity> contracts = contractRepository.findBySubordinateIdIn(subordinateIds);

        // Get contract IDs
        List<Long> contractIds = contracts.stream()
                .map(SimpleContractEntity::getId)
                .collect(Collectors.toList());

        // Get projects related to contracts
        List<SimpleProjectEntity> projectsByContract = projectRepository.findByContractIdIn(contractIds);

        // Get projects directly related to subordinates (where contract is null)
        List<SimpleProjectEntity> projectsBySubordinate = projectRepository
                .findBySubordinateIdInAndContractIdIsNull(subordinateIds);

        // Combine all project IDs
        List<Long> projectIds = new ArrayList<>();

        // Add project IDs from contracts
        projectIds.addAll(projectsByContract.stream()
                .map(SimpleProjectEntity::getId)
                .collect(Collectors.toList()));

        // Add project IDs directly related to subordinates
        projectIds.addAll(projectsBySubordinate.stream()
                .map(SimpleProjectEntity::getId)
                .collect(Collectors.toList()));

        // Remove duplicates and return
        return projectIds.stream().distinct().collect(Collectors.toList());
    }

    private List<OrganizationEntity> findAllSubordinatesRecursively(Long directorId) {
        List<OrganizationEntity> directSubordinates = organizationRepository
                .findAllSubordinatesByDirectorId(directorId);
        List<OrganizationEntity> allSubordinates = new java.util.ArrayList<>(directSubordinates);

        // Para cada subordinado direto, buscar seus subordinados recursivamente
        for (OrganizationEntity subordinate : directSubordinates) {
            List<OrganizationEntity> nestedSubordinates = findAllSubordinatesRecursively(subordinate.getId());
            allSubordinates.addAll(nestedSubordinates);
        }

        return allSubordinates;
    }


    public List<ProjectEntity> findAllByIds(List<Long> ids){
        return organizationProjectRepository.findAllById(ids);
    }
}
