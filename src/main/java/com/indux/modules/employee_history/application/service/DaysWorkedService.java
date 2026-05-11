package com.indux.modules.employee_history.application.service;

import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.modules.employee_history.application.dto.*;
import com.indux.modules.employee_history.domain.entity.DaysWorked;
import com.indux.modules.employee_history.domain.repository.DaysWorkedRepository;
import com.indux.modules.employee_history.infra.mapper.DaysWorkedMapper;
import com.indux.modules.ocf.application.dto.OccurrenceFFDTO;
import com.indux.modules.ocf.application.service.OccurrenceFFService;
import com.indux.modules.organization_chart.application.dtos.ProjectDTO;
import com.indux.modules.organization_chart.application.services.SubordinateService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;

import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationProjectRepository;
import com.indux.modules.organization_chart.infra.mapper.ProjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DaysWorkedService {

    private final DaysWorkedRepository daysWorkedRepository;
    private final DaysWorkedMapper daysWorkedMapper;
    private final EmployeeRepository employeeRepository;
    private final SubordinateService subordinateService;
    private final OrganizationProjectRepository organizationProjectRepository;
    private final ProjectMapper projectMapper;
    private final OccurrenceFFService occurrenceFFService;
    private final EmployeeEnrichmentService employeeEnrichmentService;

    public DaysWorkedService(
            DaysWorkedRepository daysWorkedRepository,
            DaysWorkedMapper daysWorkedMapper,
            EmployeeRepository employeeRepository,
            SubordinateService subordinateService,
            OrganizationProjectRepository organizationProjectRepository,
            ProjectMapper projectMapper,
            OccurrenceFFService occurrenceFFService,
            EmployeeEnrichmentService employeeEnrichmentService) {
        this.daysWorkedRepository = daysWorkedRepository;
        this.daysWorkedMapper = daysWorkedMapper;
        this.employeeRepository = employeeRepository;
        this.subordinateService = subordinateService;
        this.organizationProjectRepository = organizationProjectRepository;
        this.projectMapper = projectMapper;
        this.occurrenceFFService = occurrenceFFService;
        this.employeeEnrichmentService = employeeEnrichmentService;
    }

    public Page<DaysWordedDTO> getDaysWorked(FilterDaysWorked filter, Pageable pageable) {
        if (filter != null && filter.getFilialId() == null) {
            getFilialOrganization(filter);
        }
        Page<DaysWorked> workedEntity = daysWorkedRepository.findFilterDaysWorked(filter, pageable);

        List<DaysWordedDTO> wordedDTOS = workedEntity.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(wordedDTOS, pageable, workedEntity.getTotalElements());
    }

    private void getFilialOrganization(FilterDaysWorked filter) {
        if (filter == null) return;

        if (filter.getProjectId() != null) {
            resolveBySubordinateFilter(filter, null, null, null, null, null, filter.getProjectId());
        } else if (filter.getContractId() != null) {
            resolveBySubordinateFilter(filter, null, null, null, null, filter.getContractId(), null);
        } else if (filter.getSetorId() != null) {
            resolveBySubordinateFilter(filter, null, null, null, filter.getSetorId(), null, null);
        } else if (filter.getRegionalId() != null) {
            resolveBySubordinateFilter(filter, null, null, filter.getRegionalId(), null, null, null);
        } else if (filter.getSuperintendenciaId() != null) {
            resolveBySubordinateFilter(filter, null, filter.getSuperintendenciaId(), null, null, null, null);
        } else if (filter.getDiretoriaId() != null) {
            resolveBySubordinateFilter(filter, filter.getDiretoriaId(), null, null, null, null, null);
        } else if (filter.getFilialMegaId() != null && !filter.getFilialMegaId().isEmpty()) {
            resolveByFilialMegaId(filter);
        } else if (filter.getCentroCustoHcm() != null && !filter.getCentroCustoHcm().isEmpty()) {
            resolveByCentroCustoHcm(filter);
        }
    }

    private void resolveBySubordinateFilter(FilterDaysWorked filter,
                                             List<Long> diretoriaIds, List<Long> superintendenciaIds,
                                             List<Long> regionalIds, List<Long> setorIds,
                                             List<Long> contractIds, List<Long> projectIds) {
        var filiaisEntities = subordinateService.getFiliaisHcmByFilters(
                diretoriaIds, superintendenciaIds, regionalIds, setorIds, contractIds, projectIds);
        applyFilialIds(filter, filiaisEntities.stream()
                .map(FilialHcmEntity::getFilialId)
                .collect(Collectors.toList()));
    }

    private void resolveByFilialMegaId(FilterDaysWorked filter) {
        List<Integer> filialIds = organizationProjectRepository
                .findByFilialMegaIdIn(filter.getFilialMegaId())
                .stream()
                .flatMap(project -> project.getFilial().stream())
                .map(FilialHcmEntity::getFilialId)
                .distinct()
                .collect(Collectors.toList());
        applyFilialIds(filter, filialIds);
    }

    private void resolveByCentroCustoHcm(FilterDaysWorked filter) {
        List<Integer> filialIds = organizationProjectRepository
                .findByHcmCcIdInWithFilial(filter.getCentroCustoHcm())
                .stream()
                .flatMap(project -> project.getFilial().stream())
                .map(FilialHcmEntity::getFilialId)
                .distinct()
                .collect(Collectors.toList());
        applyFilialIds(filter, filialIds);
    }

    private void applyFilialIds(FilterDaysWorked filter, List<Integer> filialIds) {
        filter.setFilialId(filialIds.isEmpty() ? List.of(0) : filialIds);
    }

    private DaysWordedDTO mapToDTO(DaysWorked daysWorked) {
        EmployeeEnrichmentService.EmployeeInfo empInfo =
                employeeEnrichmentService.resolveEmployeeInfo(daysWorked.getRegistration());

        String employeeName = empInfo != null ? empInfo.getName() : null;
        UUID employeeId = empInfo != null ? empInfo.getId() : null;
        String cargoName = empInfo != null ? empInfo.getCargoName() : null;

        String filialName = employeeEnrichmentService.resolveFilialName(daysWorked.getFilialId());
        EmployeeSummaryDTO.HierarchyInfo hierarchyInfo =
                employeeEnrichmentService.buildHierarchyInfo(daysWorked.getFilialId());

        return daysWorkedMapper.toSingleDTO(daysWorked, filialName, employeeId, employeeName, cargoName, hierarchyInfo);
    }

    public Page<LocalDate> getCompetence(Pageable pageable) {
        return daysWorkedRepository.findDistinctCompetences(pageable);
    }

    public Page<CompetenceDTO> getCompetencesManage(FilterDaysWorked filter, Pageable pageable) {
        if (filter != null && filter.getFilialId() == null) {
            getFilialOrganization(filter);
        }

        Page<CompetenceFilialProjection> projections = daysWorkedRepository.findDistinctCompetencesByFilial(filter,
                pageable);

        List<CompetenceDTO> competenceDTOs = projections.getContent().stream()
                .map(proj -> {
                    ProjectDTO projectDTO = organizationProjectRepository
                            .findByIdWithDetails(proj.projectId())
                            .map(projectMapper::toDto)
                            .orElse(null);
                    return new CompetenceDTO(proj.competence(), projectDTO);
                })
                .filter(dto -> dto.project() != null)
                .collect(Collectors.toList());

        long eliminated = projections.getContent().size() - competenceDTOs.size();
        long adjustedTotal = projections.getTotalElements() - eliminated;

        return new PageImpl<>(competenceDTOs, pageable, adjustedTotal);
    }

    public void createAlert(CreateAlert alert, String name) {
        String registration = employeeRepository.findAllByNameIgnoreCase(alert.nameDivergent()).stream()
                .filter(e -> e.getAdmissionDate() != null)
                .max(Comparator
                        .comparing((Employee e) -> "Trabalhando".equalsIgnoreCase(e.getStatusEmployee()) ? 1 : 0)
                        .thenComparing(Employee::getAdmissionDate))
                .orElseThrow(() -> new NotFoundEmployee("Nenhum funcionário encontrado com o nome: " + alert.nameDivergent()))
                .getRegistration();

        OccurrenceFFDTO occurrenceFFDTO = OccurrenceFFDTO.forAlert(registration, alert.observation(), alert.type(), alert.competence());
        try {
            occurrenceFFService.createDerivedOccurrenceWithDetails(occurrenceFFDTO, name);
        } catch (InterruptedException e) {
            throw new ModuleFailure("Falha ao criar alerta de divergência de competência: " + e.getMessage());
        }
    }
}
