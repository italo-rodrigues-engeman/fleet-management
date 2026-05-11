package com.indux.modules.mobile.domain.gateway;

import com.indux.modules.employee_history.domain.entity.PayrollEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface MobilePayrollGateway {

    Page<Date> findDistinctCompetencesByRegistration(String registration, Pageable pageable);

    List<PayrollEntity> findByRegistrationAndCompetence(String registration, Date competenceStart, Date competenceEnd);
}
