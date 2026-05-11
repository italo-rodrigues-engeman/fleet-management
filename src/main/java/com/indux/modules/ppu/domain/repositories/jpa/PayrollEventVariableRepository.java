package com.indux.modules.ppu.domain.repositories.jpa;

import com.indux.modules.ppu.domain.entities.jpa.PayrollEventVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface PayrollEventVariableRepository extends JpaRepository<PayrollEventVariable, Long> {

    Optional<PayrollEventVariable> findByCompetenceAndPayrollEvent_Id(YearMonth competence, Long payrollEventId);

    Optional<PayrollEventVariable>  findByPayrollEvent_Id(Long payrollEventId);

}
