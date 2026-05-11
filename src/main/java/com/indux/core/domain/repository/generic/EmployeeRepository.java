package com.indux.core.domain.repository.generic;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.domain.model.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>,
        EmployeeRepositoryCustom, JpaSpecificationExecutor<Employee> {

    @Query(value = """
            SELECT DISTINCT ON (e.cpf) e.*
            FROM tb_funcionarios e
            JOIN tb_contratos c ON c.rateio_id = e.rateio_id
            WHERE
              lower(coalesce(c.nome_centro_custos, '')) LIKE '%suprimentos%'
              OR lower(coalesce(c.nome_projeto, ''))    LIKE '%suprimentos%'
            ORDER BY
              e.cpf,
              CASE WHEN lower(e.situacao) = 'trabalhando' THEN 0 ELSE 1 END,
              e.data_admissao DESC NULLS LAST
            """, countQuery = """
            SELECT COUNT(DISTINCT e.cpf)
            FROM tb_funcionarios e
            JOIN tb_contratos c ON c.rateio_id = e.rateio_id
            WHERE
              lower(coalesce(c.nome_centro_custos, '')) LIKE '%suprimentos%'
              OR lower(coalesce(c.nome_projeto, ''))    LIKE '%suprimentos%'
            """, nativeQuery = true)
    Page<Employee> findLatestByCpfInSuprimentos(Pageable pageable);

    Employee findByCpfAndStatusEmployee(String cpf, String status);

    List<Employee> findAllByPositionInAndStatusEmployeeNot(List<String> cargoIdsHcm, String statusToExclude);

    Optional<Employee> findByCpf(String cpf);

    Page<Employee> findByStatusEmployeeIgnoreCase(String statusEmployee, Pageable pageable);

    // Buscar todos os vínculos de um funcionário pelo CPF
    List<Employee> findAllByCpf(String cpf);

    // Buscar todos os vínculos de um funcionário pelo nome
    List<Employee> findAllByNameIgnoreCase(String name);

    @Query("""
                SELECT new com.indux.core.application.dto.generic.EmployeeDTO(
                    e.id,
                    e.registration,
                    e.cpf,
                    e.name,
                    e.personalEmail,
                    e.businessEmail,
                    COALESCE(cg.nameTitle, e.position),
                    CASE
                        WHEN TRIM(e.cellphone) IS NULL OR TRIM(e.cellphone) = '' OR TRIM(e.cellphone) = '0' THEN NULL
                        ELSE TRIM(e.cellphone)
                    END,
                    CASE
                        WHEN TRIM(e.cellphone2) IS NULL OR TRIM(e.cellphone2) = '' OR TRIM(e.cellphone2) = '0' THEN NULL
                        ELSE TRIM(e.cellphone2)
                    END,
                    e.branch_id,
                    f.branchName,
                    e.statusEmployee,
                    new com.indux.core.application.dto.generic.BankDetailDTO('', '', '', '', ''),
                    new com.indux.core.domain.model.employee.ContractProject(
                        c.id, c.rateio, c.costCenterName, c.megaId,
                        c.projectName, c.contractManager, c.client
                    ),
                    e.SISPAT,
                    e.costCenterId,
                    e.filialIdHcm,
                    e.birthDate,
                    e.admissionDate
                )
                FROM Employee e
                LEFT JOIN ContractProject c ON e.contract_id = c.rateio
                LEFT JOIN Filial f ON e.branch_id = f.branchId
                LEFT JOIN Cargo cg ON e.position = cg.idHcm
                WHERE e.cpf = :cpf AND e.statusEmployee = 'Trabalhando'
            """)
    List<EmployeeDTO> findByCpfAndStatus(@Param("cpf") String cpf);

    Set<Employee> findAllByRegistrationIn(List<String> registrations);

    Optional<Employee> findByRegistration(String registration);

    List<Employee> findAllByRegistration(String registration);

    @Query("""
                SELECT COUNT(e)
                FROM Employee e
                WHERE e.position = :cargoIdHcm
                AND e.filialIdHcm IN :filialIds
                AND e.statusEmployee = 'Trabalhando'
            """)
    Long countActiveEmployeesByCargoAndFiliais(@Param("cargoIdHcm") String cargoIdHcm,
            @Param("filialIds") List<Integer> filialIds);

    @Query("""
                SELECT COUNT(e)
                FROM Employee e
                WHERE e.position = :cargoIdHcm
                AND e.filialIdHcm IN :filialIds
            """)
    Long countAllEmployeesByCargoAndFiliais(@Param("cargoIdHcm") String cargoIdHcm,
            @Param("filialIds") List<Integer> filialIds);

}
