package com.indux.modules.modulo_mega.domain.repository.jpa;

import com.indux.modules.modulo_mega.domain.entities.jpa.CodigoAplicacaoMegaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CodigoAplicacaoMegaRepository extends JpaRepository<CodigoAplicacaoMegaEntity, Long> {
    
    @Query("SELECT c FROM CodigoAplicacaoMegaEntity c WHERE LOWER(c.descAplicacao) LIKE LOWER(CONCAT('%', :descricao, '%')) ORDER BY c.codAplicacao ASC")
    Page<CodigoAplicacaoMegaEntity> findByDescAplicacaoContainingIgnoreCase(@Param("descricao") String descricao, Pageable pageable);
    
    @Query("SELECT c FROM CodigoAplicacaoMegaEntity c WHERE CAST(c.codAplicacao AS string) LIKE CONCAT('%', :codigo, '%') ORDER BY c.codAplicacao ASC")
    Page<CodigoAplicacaoMegaEntity> findByCodAplicacaoContaining(@Param("codigo") String codigo, Pageable pageable);
    
    @Query("SELECT c FROM CodigoAplicacaoMegaEntity c WHERE " +
           "LOWER(c.descAplicacao) LIKE LOWER(CONCAT('%', :descricao, '%')) AND " +
           "CAST(c.codAplicacao AS string) LIKE CONCAT('%', :codigo, '%') " +
           "ORDER BY c.codAplicacao ASC")
    Page<CodigoAplicacaoMegaEntity> findByDescAplicacaoAndCodAplicacaoContaining(
            @Param("descricao") String descricao, 
            @Param("codigo") String codigo, 
            Pageable pageable);
}

