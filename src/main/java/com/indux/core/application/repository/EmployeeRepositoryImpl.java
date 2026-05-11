package com.indux.core.application.repository;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.domain.repository.generic.EmployeeRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@CacheConfig(cacheNames = "employees")
public class EmployeeRepositoryImpl implements EmployeeRepositoryCustom {
  @PersistenceContext
  private EntityManager em;

  @Override
  @Cacheable
  public List<EmployeeDTO> search(String filter,
      boolean showBank,
      boolean showDemitido) {
    String cleaned = filter == null ? "" : filter.replaceAll("\\D", "");

    String select = """
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
              %s,
              new com.indux.core.domain.model.employee.ContractProject(
                c.id,
                c.rateio,
                c.costCenterName,
                c.megaId,
                c.projectName,
                c.contractManager,
                c.client
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
            LEFT JOIN BankDetails b ON e.registration = b.matricula
        """;

    String bankPart = """
        new com.indux.core.application.dto.generic.BankDetailDTO(
          CASE WHEN :showBank = true THEN COALESCE(b.numero_banco,'') ELSE '' END,
          CASE WHEN :showBank = true THEN COALESCE(b.agencia,'') ELSE '' END,
          CASE WHEN :showBank = true THEN COALESCE(b.conta_bancaria,'') ELSE '' END,
          CASE
            WHEN :showBank = false OR b.tipo_conta IS NULL THEN ''
            WHEN b.tipo_conta = 1 THEN 'Conta Corrente'
            WHEN b.tipo_conta = 2 THEN 'Conta Poupança'
            WHEN b.tipo_conta = 3 THEN 'Conta Salário'
            ELSE 'Outros'
          END,
          CASE WHEN :showBank = true THEN COALESCE(b.nome_banco,'') ELSE '' END
        )
        """;

    StringBuilder jpql = new StringBuilder(String.format(select, bankPart));

    jpql.append("WHERE (:showDemitido = true OR e.statusEmployee = 'Trabalhando') ");
    if (!cleaned.isBlank() && cleaned.matches("\\d+")) {
      jpql.append("AND ( " +
          "e.registration = :cleaned " +
          "OR REPLACE(REPLACE(e.cpf, '.', ''), '-', '') = :cleaned " +
          "OR REPLACE(REPLACE(REPLACE(CAST(e.birthDate AS string), '-', ''), '/', ''), '.', '') = :cleaned " +
          ") ");
    } else {
      jpql.append("AND ( " +
          "LOWER(e.name) LIKE CONCAT('%', LOWER(:filter), '%') " +
          "OR LOWER(e.cpf) LIKE CONCAT('%', LOWER(:filter), '%') " +
          "OR LOWER(f.branchName) LIKE CONCAT('%', LOWER(:filter), '%') " +
          "OR LOWER(c.costCenterName) LIKE CONCAT('%', LOWER(:filter), '%') " +
          "OR LOWER(c.projectName) LIKE CONCAT('%', LOWER(:filter), '%') " +
          "OR LOWER(cg.nameTitle) LIKE CONCAT('%', LOWER(:filter), '%') " +
          ") ");
    }

    TypedQuery<EmployeeDTO> q = em.createQuery(jpql.toString(), EmployeeDTO.class);
    q.setParameter("showDemitido", showDemitido);
    q.setParameter("showBank", showBank);
    if (!cleaned.isBlank() && cleaned.matches("\\d+")) {
      q.setParameter("cleaned", cleaned);
    } else {
      q.setParameter("filter", filter.trim());
    }

    return q.getResultList();
  }
}
