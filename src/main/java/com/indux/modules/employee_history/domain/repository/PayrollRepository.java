package com.indux.modules.employee_history.domain.repository;

import com.indux.modules.employee_history.domain.entity.PayrollEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollRepository extends JpaRepository<PayrollEntity, String>, PayrollRepositoryCustom {
}
