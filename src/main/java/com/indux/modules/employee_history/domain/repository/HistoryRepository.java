package com.indux.modules.employee_history.domain.repository;

import com.indux.core.domain.model.employee.EmployeePosition;
import com.indux.modules.employee_history.domain.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History, String>, HistoryRepositoryCustom {
}
