package com.indux.modules.mobile.application.service;

import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.modules.mobile.application.dto.response.MobilePayrollDetailResponse;
import com.indux.modules.mobile.application.dto.response.MobilePayrollSummaryResponse;
import com.indux.modules.mobile.application.mapper.MobilePayrollMapper;
import com.indux.modules.mobile.domain.gateway.MobilePayrollGateway;
import com.indux.modules.mobile.infra.exception.MobilePayrollNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
public class MobilePayrollService {

    private final MobilePayrollGateway mobilePayrollGateway;
    private final EmployeeRepository employeeRepository;
    private final MobilePayrollMapper mobilePayrollMapper;

    public MobilePayrollService(MobilePayrollGateway mobilePayrollGateway,
                                EmployeeRepository employeeRepository,
                                MobilePayrollMapper mobilePayrollMapper) {
        this.mobilePayrollGateway = mobilePayrollGateway;
        this.employeeRepository = employeeRepository;
        this.mobilePayrollMapper = mobilePayrollMapper;
    }

    public Page<MobilePayrollSummaryResponse> getMyPayrollCompetences(String cpf, Pageable pageable) {
        String registration = resolveRegistration(cpf);
        return mobilePayrollGateway.findDistinctCompetencesByRegistration(registration, pageable)
                .map(mobilePayrollMapper::toSummary);
    }

    public MobilePayrollDetailResponse getMyPayrollByCompetence(String cpf, YearMonth competence) {
        String registration = resolveRegistration(cpf);

        Date competenceStart = Date.from(competence.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC));
        Date competenceEnd = Date.from(competence.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC));

        List<com.indux.modules.employee_history.domain.entity.PayrollEntity> payrollItems =
                mobilePayrollGateway.findByRegistrationAndCompetence(registration, competenceStart, competenceEnd);

        if (payrollItems.isEmpty()) {
            throw new MobilePayrollNotFoundException("Competência de folha não encontrada para o colaborador autenticado.");
        }

        return MobilePayrollDetailResponse.builder()
                .competence(payrollItems.getFirst().getCompetence())
                .events(payrollItems.stream().map(mobilePayrollMapper::toEvent).toList())
                .build();
    }

    private String resolveRegistration(String cpf) {
        Employee employee = employeeRepository.findByCpf(cpf)
                .orElseThrow(() -> new MobilePayrollNotFoundException("Colaborador não encontrado para o CPF autenticado."));

        if (employee.getRegistration() == null || employee.getRegistration().isBlank()) {
            throw new MobilePayrollNotFoundException("Colaborador autenticado sem matrícula válida.");
        }

        return employee.getRegistration();
    }
}
