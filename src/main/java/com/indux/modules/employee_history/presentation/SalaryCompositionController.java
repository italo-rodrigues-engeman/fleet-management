package com.indux.modules.employee_history.presentation;

import com.indux.modules.employee_history.application.dto.FilterSalaryComposition;
import com.indux.modules.employee_history.application.dto.SalaryCompositionResponse;
import com.indux.modules.employee_history.application.service.SalaryCompositionService;
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
@RequestMapping("/api/salary-composition")
@Validated
public class SalaryCompositionController {

    private final SalaryCompositionService salaryCompositionService;

    public SalaryCompositionController(SalaryCompositionService salaryCompositionService) {
        this.salaryCompositionService = salaryCompositionService;
    }

    @GetMapping
    public Page<SalaryCompositionResponse> getByFilter(
            FilterSalaryComposition filter,
            Pageable pageable,
            @RequestParam String moduleId,
            JwtAuthenticationToken token
    ) {
        UUID userId = UUID.fromString(token.getName());
        UUID modId = UUID.fromString(moduleId);
        return salaryCompositionService.getByFilter(filter, pageable, userId, modId);
    }
}
