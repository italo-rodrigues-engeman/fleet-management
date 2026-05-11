package com.indux.modules.contracts.domain.repository;

import com.indux.modules.contracts.domain.model.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    
    @Query(value = "SELECT c.* FROM tb_contratos c " +
           "WHERE (:cliente IS NULL OR c.cliente::text ILIKE '%' || :cliente || '%') " +
           "AND (:nomeProjeto IS NULL OR c.nome_projeto::text ILIKE '%' || :nomeProjeto || '%') " +
           "AND (:regional IS NULL OR c.regional::text ILIKE '%' || :regional || '%') " +
           "AND (:ativo IS NULL OR c.ativo = :ativo) " +
           "AND (:os IS NULL OR c.os::text ILIKE '%' || :os || '%') " +
           "AND (:codSap IS NULL OR c.cod_sap::text ILIKE '%' || :codSap || '%') " +
           "AND (:gestorInterno IS NULL OR c.gestor_interno_contrato::text ILIKE '%' || :gestorInterno || '%') " +
           "AND (:gestorCliente IS NULL OR c.gestor_cliente_contrato::text ILIKE '%' || :gestorCliente || '%') " +
           "ORDER BY c.id", 
           countQuery = "SELECT COUNT(c.id) FROM tb_contratos c " +
           "WHERE (:cliente IS NULL OR c.cliente::text ILIKE '%' || :cliente || '%') " +
           "AND (:nomeProjeto IS NULL OR c.nome_projeto::text ILIKE '%' || :nomeProjeto || '%') " +
           "AND (:regional IS NULL OR c.regional::text ILIKE '%' || :regional || '%') " +
           "AND (:ativo IS NULL OR c.ativo = :ativo) " +
           "AND (:os IS NULL OR c.os::text ILIKE '%' || :os || '%') " +
           "AND (:codSap IS NULL OR c.cod_sap::text ILIKE '%' || :codSap || '%') " +
           "AND (:gestorInterno IS NULL OR c.gestor_interno_contrato::text ILIKE '%' || :gestorInterno || '%') " +
           "AND (:gestorCliente IS NULL OR c.gestor_cliente_contrato::text ILIKE '%' || :gestorCliente || '%')",
           nativeQuery = true)
    Page<Contract> findAllWithFilters(
            @Param("cliente") String cliente,
            @Param("nomeProjeto") String nomeProjeto,
            @Param("regional") String regional,
            @Param("ativo") Boolean ativo,
            @Param("os") String os,
            @Param("codSap") String codSap,
            @Param("gestorInterno") String gestorInterno,
            @Param("gestorCliente") String gestorCliente,
            Pageable pageable);

    @Query(value = "SELECT c.* FROM tb_contratos c " +
           "INNER JOIN tb_filiais f ON c.filial_id = f.codigo_filial " +
           "INNER JOIN tb_regional r ON f.Id_regional = r.id " +
           "WHERE r.id = :regionalId " +
           "ORDER BY c.id",
           countQuery = "SELECT COUNT(c.id) FROM tb_contratos c " +
           "INNER JOIN tb_filiais f ON c.filial_id = f.codigo_filial " +
           "INNER JOIN tb_regional r ON f.Id_regional = r.id " +
           "WHERE r.id = :regionalId",
           nativeQuery = true)
    Page<Contract> findByRegionalId(@Param("regionalId") Long regionalId, Pageable pageable);

    @Query(value = "SELECT c.* FROM tb_contratos c " +
           "INNER JOIN tb_filiais f ON c.filial_id = f.codigo_filial " +
           "INNER JOIN tb_regional r ON f.Id_regional = r.id " +
           "WHERE r.id IN :regionalIds " +
           "ORDER BY c.id",
           countQuery = "SELECT COUNT(c.id) FROM tb_contratos c " +
           "INNER JOIN tb_filiais f ON c.filial_id = f.codigo_filial " +
           "INNER JOIN tb_regional r ON f.Id_regional = r.id " +
           "WHERE r.id IN :regionalIds",
           nativeQuery = true)
    Page<Contract> findByRegionalIds(@Param("regionalIds") java.util.List<Long> regionalIds, Pageable pageable);

    @Query(value = "SELECT c.* FROM tb_contratos c " +
           "INNER JOIN tb_filiais f ON c.filial_id = f.codigo_filial " +
           "INNER JOIN tb_regional r ON f.Id_regional = r.id " +
           "WHERE r.regional = :regionalName " +
           "ORDER BY c.id",
           countQuery = "SELECT COUNT(c.id) FROM tb_contratos c " +
           "INNER JOIN tb_filiais f ON c.filial_id = f.codigo_filial " +
           "INNER JOIN tb_regional r ON f.Id_regional = r.id " +
           "WHERE r.regional = :regionalName",
           nativeQuery = true)
    Page<Contract> findByRegionalName(@Param("regionalName") String regionalName, Pageable pageable);
} 