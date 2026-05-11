package com.indux.core.domain.repository.generic;

import com.indux.core.domain.exception.DuplicateContractException;
import com.indux.core.domain.model.employee.ContractProject;
import jakarta.validation.constraints.NotNull;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@CacheConfig(cacheNames = "contractProjects")
public interface ContractProjectRepository extends JpaRepository<ContractProject, Integer> {
    @Cacheable(key = "#rateio")
    @Query("""
            SELECT c FROM ContractProject c
            LEFT JOIN FETCH c.filial
            LEFT JOIN FETCH c.coordinators
            WHERE c.rateio = :rateio
            """)
    List<ContractProject> findAllByRateio(@Param("rateio") Integer rateio);

    default Optional<ContractProject> findWithFilialByRateio(@Param("rateio") Integer rateio) {
        List<ContractProject> contracts = findAllByRateio(rateio);
        if (contracts.size() > 1) {
            String errorMessage = String.format(
                "Existem múltiplos contratos com o mesmo rateio_id %d:%n",
                rateio
            );
            for (ContractProject contract : contracts) {
                errorMessage += String.format(
                    "- Contrato ID: %d, Nome do Projeto: %s, Centro de Custo: %s%n",
                    contract.getId(),
                    contract.getProjectName(),
                    contract.getCostCenterName()
                );
            }
            throw new DuplicateContractException(errorMessage);
        }
        return contracts.isEmpty() ? Optional.empty() : Optional.of(contracts.get(0));
    }

    @Cacheable(key = "#rateio + '_simple'")
    Optional<ContractProject> findByRateio(Integer rateio);

    @Cacheable(key = "'allWithFilial'")
    @Query("SELECT c FROM ContractProject c LEFT JOIN FETCH c.filial LEFT JOIN FETCH c.coordinators")
    List<ContractProject> findAllWithFilial();

    @Cacheable(key = "#filialCodes")
    @Query("SELECT c FROM ContractProject c LEFT JOIN FETCH c.coordinators WHERE c.filial.branchId IN :filialCodes")
    List<ContractProject> findAllByFilialCodigoFilialIn(Collection<Integer> filialCodes);

    @Cacheable(key = "#filialCodes + '_ativo'")
    @Query("SELECT c FROM ContractProject c LEFT JOIN FETCH c.coordinators WHERE c.filial.branchId IN :filialCodes AND c.ativo = true")
    List<ContractProject> findAllByFilialCodigoFilialInAndAtivoTrue(Collection<Integer> filialCodes);

    @Cacheable(key = "'allWithFilial_ativo'")
    @Query("SELECT c FROM ContractProject c LEFT JOIN FETCH c.filial LEFT JOIN FETCH c.coordinators WHERE c.ativo = true")
    List<ContractProject> findAllWithFilialAndAtivoTrue();

    @NotNull
    @Override
    @Cacheable(key = "'all'")
    List<ContractProject> findAll();
    
    @Cacheable(key = "'allWithCoordinators'")
    @Query("SELECT c FROM ContractProject c LEFT JOIN FETCH c.filial LEFT JOIN FETCH c.coordinators")
    List<ContractProject> findAllWithCoordinators();

    List<ContractProject> findAllByRateioIn(Set<Integer> ids);

    @Query(value = "SELECT c.* FROM tb_contratos c " +
            "WHERE c.id_cliente = :clientId " +
            "AND c.ativo = true " +
            "ORDER BY c.data_assinatura DESC", nativeQuery = true)
    List<ContractProject> findAllByClientId(@Param("clientId") Long clientId);

    @Query("SELECT c FROM ContractProject c " +
            "WHERE (:nome IS NULL OR LOWER(c.costCenterName) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
            "AND (:apenasAtivos = false OR c.ativo = true)")
    List<ContractProject> findResumoContratos(
            @Param("nome") String nome,
            @Param("apenasAtivos") boolean apenasAtivos
    );
}
