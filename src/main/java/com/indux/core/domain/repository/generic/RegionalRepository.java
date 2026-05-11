package com.indux.core.domain.repository.generic;

import com.indux.core.domain.model.employee.Regional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionalRepository extends JpaRepository<Regional, Long> {

    /**
     * Busca uma regional por ID incluindo suas filiais
     */
    @Query("SELECT DISTINCT r FROM Regional r LEFT JOIN FETCH r.filiais WHERE r.id = :id")
    Optional<Regional> findByIdWithFiliais(@Param("id") Long id);

    /**
     * Busca todas as regionais com suas filiais
     */
    @Query("SELECT DISTINCT r FROM Regional r LEFT JOIN FETCH r.filiais")
    List<Regional> findAllWithFiliais();

    /**
     * Busca todas as regionais com suas filiais ordenadas por ID
     */
    @Query("SELECT DISTINCT r FROM Regional r LEFT JOIN FETCH r.filiais ORDER BY r.id")
    List<Regional> findAllWithFiliaisOrdered();

    /**
     * Busca regionais que tenham filiais associadas
     */
    @Query("SELECT DISTINCT r FROM Regional r LEFT JOIN FETCH r.filiais WHERE r.filiais IS NOT EMPTY")
    List<Regional> findRegionaisWithFiliais();

    /**
     * Busca regional por nome
     */
    Optional<Regional> findByRegional(String regional);

    /**
     * Busca regionais que contenham o nome especificado
     */
    List<Regional> findByRegionalContainingIgnoreCase(String regional);

    /**
     * Busca regional por filial (método legado para compatibilidade)
     */
    Optional<Regional> findByFilial(Integer filial);

    /**
     * Busca regional por filial usando a nova relação (método alternativo)
     */
    @Query("SELECT r FROM Regional r JOIN r.filiais f WHERE f.branchId = :filialId")
    Optional<Regional> findByFilialId(@Param("filialId") Long filialId);

    /**
     * Verifica se uma regional tem filiais associadas
     */
    @Query("SELECT COUNT(f) > 0 FROM Regional r JOIN r.filiais f WHERE r.id = :regionalId")
    boolean hasFiliais(@Param("regionalId") Long regionalId);
} 