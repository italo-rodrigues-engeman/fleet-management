package com.indux.modules.employee_history.application.service;

import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.core.domain.model.modules.ModulePermission;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.modules.employee_history.application.dto.DaysWordedDTO;
import com.indux.modules.employee_history.application.dto.FilterDaysWorked;
import com.indux.modules.employee_history.application.dto.FilterSalaryComposition;
import com.indux.modules.employee_history.application.dto.SalaryCompositionResponse;
import com.indux.modules.employee_history.domain.repository.SalaryCompositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SalaryCompositionService {

    private final SalaryCompositionRepository salaryCompositionRepository;
    private final EmployeeEnrichmentService employeeEnrichmentService;
    private final PayrollAccessAuthorizationService authorizationService;
    private final ModuleManagementService moduleService;
    private final DaysWorkedService  daysWorkedService;

    public SalaryCompositionService(SalaryCompositionRepository salaryCompositionRepository,
                                    EmployeeEnrichmentService employeeEnrichmentService,
                                    PayrollAccessAuthorizationService authorizationService,
                                    ModuleManagementService moduleService, DaysWorkedService daysWorkedService) {
        this.salaryCompositionRepository = salaryCompositionRepository;
        this.employeeEnrichmentService = employeeEnrichmentService;
        this.authorizationService = authorizationService;
        this.moduleService = moduleService;
        this.daysWorkedService = daysWorkedService;
    }

    public Page<SalaryCompositionResponse> getByFilter(FilterSalaryComposition filter, Pageable pageable,
                                                       UUID userId, UUID moduleId) {
        Modulo module = moduleService.getModuleByID(moduleId);
        ModulePermission permission = authorizationService.findUserPermission(module, userId);
        int userLevel = authorizationService.resolveUserMaxLevel(permission);
        Set<Integer> allowedFiliais = authorizationService.resolveAllowedFiliais(permission);
        String userRegistration = authorizationService.resolveUserRegistration(userId);

        if (filter.getFilialIds() != null && !filter.getFilialIds().isEmpty()) {
            List<Long> filialLongs = filter.getFilialIds().stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            authorizationService.validateFilterFiliais(allowedFiliais, filialLongs);
        }
        
        if (filter.getCompetence() != null) {
            FilterDaysWorked filterDaysWorked = new FilterDaysWorked();
            filterDaysWorked.setCompetence(filter.getCompetence());
            List<Integer> filialIds = filter.getFilialIds()
                    .stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());

            filterDaysWorked.setFilialId(filialIds);
            Pageable pageableDays = PageRequest.of(0, 1500);
            Page<DaysWordedDTO> daysWordedDTOS = daysWorkedService.getDaysWorked(filterDaysWorked, pageableDays);

            List<String> registrations = daysWordedDTOS.getContent().stream()
                    .map(DaysWordedDTO::registration)
                    .distinct()
                    .collect(Collectors.toList());

            filter.setRegistrations(registrations);
            filter.setFilialIds(null);
        }

        Page<SalaryCompositionResponse> page = salaryCompositionRepository.findByFilter(filter, pageable);

        List<SalaryCompositionResponse> enriched = page.getContent().stream()
                .map(this::enrich)
                .toList();

        List<SalaryCompositionResponse> postFiltered = applyPostEnrichmentFilters(enriched, filter);

        List<SalaryCompositionResponse> filtered = authorizationService.filterSuperiors(
                postFiltered,
                SalaryCompositionResponse::getRegistration,
                userLevel,
                userRegistration,
                module
        );

        return new PageImpl<>(filtered, pageable, page.getTotalElements());
    }

    private SalaryCompositionResponse enrich(SalaryCompositionResponse response) {
        EmployeeEnrichmentService.EmployeeInfo empInfo =
                employeeEnrichmentService.resolveEmployeeInfo(response.getRegistration());

        if (empInfo != null) {
            response.setEmployeeName(empInfo.getName());
            response.setCargoName(empInfo.getCargoName());
            response.setStatus(empInfo.getStatus());
            response.setEmployeeId(empInfo.getId());

            Long filialId = empInfo.getFilialIdHcm() != null
                    ? empInfo.getFilialIdHcm().longValue()
                    : null;

            EmployeeSummaryDTO.HierarchyInfo hierarchyInfo =
                    employeeEnrichmentService.buildHierarchyInfo(filialId);
            response.setHierarchyInfo(hierarchyInfo);
        }

        return response;
    }

    private List<SalaryCompositionResponse> applyPostEnrichmentFilters(List<SalaryCompositionResponse> items,
                                                                       FilterSalaryComposition filter) {
        return items.stream()
                .filter(item -> {
                    if (filter.getEmployeeName() != null && !filter.getEmployeeName().isBlank()) {
                        if (item.getEmployeeName() == null ||
                                !item.getEmployeeName().toLowerCase()
                                        .contains(filter.getEmployeeName().toLowerCase())) {
                            return false;
                        }
                    }
                    if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                        if (item.getStatus() == null ||
                                !item.getStatus().equalsIgnoreCase(filter.getStatus())) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();
    }
}
