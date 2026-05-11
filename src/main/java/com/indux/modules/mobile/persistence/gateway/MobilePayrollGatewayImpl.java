package com.indux.modules.mobile.persistence.gateway;

import com.indux.modules.employee_history.domain.entity.PayrollEntity;
import com.indux.modules.mobile.domain.gateway.MobilePayrollGateway;
import com.indux.modules.mobile.persistence.repository.MobilePayrollRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MobilePayrollGatewayImpl implements MobilePayrollGateway {

    private final MobilePayrollRepository mobilePayrollRepository;

    public MobilePayrollGatewayImpl(MobilePayrollRepository mobilePayrollRepository) {
        this.mobilePayrollRepository = mobilePayrollRepository;
    }

    @Override
    public Page<Date> findDistinctCompetencesByRegistration(String registration, Pageable pageable) {
        return mobilePayrollRepository.findDistinctCompetencesByRegistration(registration, pageable);
    }

    @Override
    public List<PayrollEntity> findByRegistrationAndCompetence(String registration, Date competenceStart, Date competenceEnd) {
        return mobilePayrollRepository.findByRegistrationAndCompetenceBetweenOrderByValueDesc(registration, competenceStart, competenceEnd);
    }
}
