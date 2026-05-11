package com.indux.modules.employee_history.presentation;

import com.indux.modules.employee_history.application.dto.FilterPayroll;
import com.indux.modules.employee_history.application.dto.PayrollResponse;
import com.indux.modules.employee_history.application.service.PayrollService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/payroll")
@Validated
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    public Page<PayrollResponse> getPayroll(
            FilterPayroll filter,
            Pageable pageable,
            @RequestParam String moduleId,
            JwtAuthenticationToken token
    ) {
        UUID userId = UUID.fromString(token.getName());
        UUID modId = UUID.fromString(moduleId);
        return payrollService.getPayroll(filter, pageable, userId, modId);
    }

    @GetMapping("/event-name")
    public Page<String> getDistinctEventNames(
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return payrollService.getDistinctEventNames(search, pageable);
    }

    @GetMapping("/event-description")
    public Page<String> getDistinctEventDescriptions(Pageable pageable) {
        return payrollService.getDistinctEventDescriptions(pageable);
    }
}
