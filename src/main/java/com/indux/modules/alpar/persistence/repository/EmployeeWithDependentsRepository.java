package com.indux.modules.alpar.persistence.repository;

import com.indux.modules.alpar.persistence.model.EmployeeWithDependentsView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EmployeeWithDependentsRepository extends JpaRepository<EmployeeWithDependentsView, String> {

        @Query(value = """
                        SELECT * FROM vw_funcionarios_com_dependentes e
                        WHERE (CAST(:cpf AS TEXT) IS NULL OR e.cpf = :cpf)
                          AND (CAST(:birthDate AS DATE) IS NULL OR e.data_nascimento = CAST(:birthDate AS DATE))
                        """, countQuery = """
                        SELECT COUNT(*) FROM vw_funcionarios_com_dependentes e
                        WHERE (CAST(:cpf AS TEXT) IS NULL OR e.cpf = :cpf)
                          AND (CAST(:birthDate AS DATE) IS NULL OR e.data_nascimento = CAST(:birthDate AS DATE))
                        """, nativeQuery = true)
        Page<EmployeeWithDependentsView> findByFilter(
                        @Param("cpf") String cpf,
                        @Param("birthDate") LocalDate birthDate,
                        Pageable pageable);
}
