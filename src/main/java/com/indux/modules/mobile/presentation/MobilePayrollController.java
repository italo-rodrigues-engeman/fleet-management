package com.indux.modules.mobile.presentation;

import com.indux.modules.mobile.application.dto.response.MobilePayrollDetailResponse;
import com.indux.modules.mobile.application.dto.response.MobilePayrollSummaryResponse;
import com.indux.modules.mobile.application.service.MobilePayrollService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/mobile/payroll")
public class MobilePayrollController {

    private final MobilePayrollService mobilePayrollService;

    public MobilePayrollController(MobilePayrollService mobilePayrollService) {
        this.mobilePayrollService = mobilePayrollService;
    }

    @GetMapping("/me")
    public Page<MobilePayrollSummaryResponse> getMyPayrollCompetences(Pageable pageable, JwtAuthenticationToken jwt) {
        String cpf = jwt.getToken().getClaimAsString("cpf");
        return mobilePayrollService.getMyPayrollCompetences(cpf, pageable);
    }

    @GetMapping("/me/{competence}")
    public MobilePayrollDetailResponse getMyPayrollByCompetence(@PathVariable String competence, JwtAuthenticationToken jwt) {
        String cpf = jwt.getToken().getClaimAsString("cpf");
        return mobilePayrollService.getMyPayrollByCompetence(cpf, parseCompetence(competence));
    }

    private YearMonth parseCompetence(String competence) {
        try {
            return YearMonth.parse(competence);
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(BAD_REQUEST, "Competência inválida. Use o formato yyyy-MM.");
        }
    }
}
