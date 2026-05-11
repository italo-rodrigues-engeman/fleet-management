package com.indux.core.domain.repository.employee;

import com.indux.core.domain.model.employee.EmployeePosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EmployeePositionRepository extends JpaRepository<EmployeePosition, Long> {
    EmployeePosition findByIdHCM(String id);
    List<EmployeePosition> findAllByIdHCMIn(Collection<String> ids);
    @Query("""
        SELECT p FROM EmployeePosition p
        WHERE LOWER(p.name) IN :names
    """)
    List<EmployeePosition> findAllByNamesIgnoreCase(@Param("names") Collection<String> namesLowerCased);

    @Query(value = """
        SELECT DISTINCT ON (nome_cargo) *
        FROM tb_cargos
        ORDER BY nome_cargo, id
        """,
                countQuery = """
        SELECT COUNT(DISTINCT nome_cargo)
        FROM tb_cargos
        """,
    nativeQuery = true)
    Page<EmployeePosition> findAllDistinctByName(Pageable pageable);

    @Query(value = """
    SELECT DISTINCT ON (nome_cargo) *
    FROM tb_cargos
    WHERE LOWER(nome_cargo) ILIKE LOWER(CONCAT('%', :name, '%'))
    ORDER BY nome_cargo, id
    """,
            countQuery = """
    SELECT COUNT(DISTINCT nome_cargo)
    FROM tb_cargos
    WHERE LOWER(nome_cargo) ILIKE LOWER(CONCAT('%', :name, '%'))
    """,
            nativeQuery = true)
    Page<EmployeePosition> findDistinctByNameLikeIgnoreCase(@Param("name") String name, Pageable pageable);

    List<EmployeePosition> findByCodeCbo(String codeCbo);
    List<EmployeePosition> findByCodeCboIn(Collection<String> codeCbo);


}
