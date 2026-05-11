package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.Atendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtendenteRepository extends JpaRepository<Atendente, Long> {
    
    Optional<Atendente> findByEmail(String email);
    
    @Query("SELECT a FROM Atendente a WHERE a.regional LIKE %:regional%")
    List<Atendente> findByRegional(@Param("regional") String regional);
    
    // Buscar atendentes vinculados a um contrato via times (tb_times_* tables)
    @Query(value = """
            SELECT a.*
            FROM tb_atendentes a
            JOIN tb_times_atendentes ta ON ta.atendente_id = a.account_id
            JOIN tb_times_contratos tc ON tc.time_id = ta.time_id
            WHERE tc.contrato_id = CAST(:contrato AS BIGINT)
            """, nativeQuery = true)
    List<Atendente> findByContrato(@Param("contrato") String contrato);
    
    // Buscar atendentes por regional (substring no JSON) e contrato via times
    @Query(value = """
            SELECT a.*
            FROM tb_atendentes a
            JOIN tb_times_atendentes ta ON ta.atendente_id = a.account_id
            JOIN tb_times_contratos tc ON tc.time_id = ta.time_id
            WHERE a.regional LIKE CONCAT('%', :regional, '%')
              AND tc.contrato_id = CAST(:contrato AS BIGINT)
            """, nativeQuery = true)
    List<Atendente> findByRegionalAndContrato(@Param("regional") String regional, @Param("contrato") String contrato);
    
    boolean existsByEmail(String email);
} 