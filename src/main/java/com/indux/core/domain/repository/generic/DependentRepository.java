package com.indux.core.domain.repository.generic;

import com.indux.core.domain.model.employee.Dependent;
import com.indux.core.domain.model.employee.Dependent.DependentPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DependentRepository extends JpaRepository<Dependent, DependentPK> {

    List<Dependent> findByEmployeeRegistrationAndPlanNotContainingAndExclusionMonth(String employeeRegistration, String inativo, String exclusionMonth);

    @Query(value = """
              SELECT * 
              FROM tb_dependentes d
              WHERE d.matricula_funcionario = :registration
                AND d.plano NOT LIKE '%#Inativo%'
                AND (CAST(d.mes_exclusao AS DATE) >= CURRENT_DATE 
                     OR CAST(d.mes_exclusao AS DATE) = CAST('1900-12-31' AS DATE))
            """, nativeQuery = true)
    List<Dependent> findActivePlansAndDependents(@Param("registration") String registration);
} 