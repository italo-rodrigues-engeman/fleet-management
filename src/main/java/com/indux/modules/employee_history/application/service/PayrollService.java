package com.indux.modules.employee_history.application.service;

import com.indux.core.domain.model.modules.ModulePermission;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.modules.employee_history.application.dto.FilterPayroll;
import com.indux.modules.employee_history.application.dto.PayrollResponse;
import com.indux.modules.employee_history.domain.repository.PayrollRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeEnrichmentService employeeEnrichmentService;
    private final PayrollAccessAuthorizationService authorizationService;
    private final ModuleManagementService moduleService;

    public PayrollService(PayrollRepository payrollRepository,
                          EmployeeEnrichmentService employeeEnrichmentService,
                          PayrollAccessAuthorizationService authorizationService,
                          ModuleManagementService moduleService) {
        this.payrollRepository = payrollRepository;
        this.employeeEnrichmentService = employeeEnrichmentService;
        this.authorizationService = authorizationService;
        this.moduleService = moduleService;
    }

    public Page<PayrollResponse> getPayroll(FilterPayroll filter, Pageable pageable,
                                            UUID userId, UUID moduleId) {
        Modulo module = moduleService.getModuleByID(moduleId);
        ModulePermission permission = authorizationService.findUserPermission(module, userId);
        int userLevel = authorizationService.resolveUserMaxLevel(permission);
        Set<Integer> allowedFiliais = authorizationService.resolveAllowedFiliais(permission);
        String userRegistration = authorizationService.resolveUserRegistration(userId);

        if (filter.getFilialHcm() != null && !filter.getFilialHcm().isEmpty()) {
            authorizationService.validateFilterFiliais(allowedFiliais, filter.getFilialHcm());
        }

        Page<PayrollResponse> page = payrollRepository.findFilterPayroll(filter, pageable);

        List<PayrollResponse> enriched = page.getContent().stream()
                .map(this::enrich)
                .toList();

        List<PayrollResponse> postFiltered = applyPostEnrichmentFilters(enriched, filter);

        List<PayrollResponse> filtered = authorizationService.filterSuperiors(
                postFiltered,
                PayrollResponse::getRegistration,
                userLevel,
                userRegistration,
                module
        );

        return new PageImpl<>(filtered, pageable, page.getTotalElements());
    }

    public Page<String> getDistinctEventNames(String search, Pageable pageable) {
        return payrollRepository.findDistinctEventNames(search, pageable);
    }

    public Page<String> getDistinctEventDescriptions(Pageable pageable) {
        return payrollRepository.findDistinctEventDescriptions(pageable);
    }

    private List<PayrollResponse> applyPostEnrichmentFilters(List<PayrollResponse> items,
                                                              FilterPayroll filter) {
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

    private PayrollResponse enrich(PayrollResponse response) {
        EmployeeEnrichmentService.EmployeeInfo empInfo =
                employeeEnrichmentService.resolveEmployeeInfo(response.getRegistration());

        if (empInfo != null) {
            response.setEmployeeId(empInfo.getId());
            response.setEmployeeName(empInfo.getName());
            response.setPositionName(empInfo.getCargoName());
            response.setStatus(empInfo.getStatus());
        }

        return response;
    }
}
