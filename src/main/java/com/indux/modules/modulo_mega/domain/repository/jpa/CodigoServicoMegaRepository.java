package com.indux.modules.modulo_mega.domain.repository.jpa;

import com.indux.modules.modulo_mega.domain.entities.jpa.CodigoServicoMegaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CodigoServicoMegaRepository extends JpaRepository<CodigoServicoMegaEntity, Long> {
    
    @Query("SELECT c FROM CodigoServicoMegaEntity c WHERE LOWER(c.descServico) LIKE LOWER(CONCAT('%', :descricao, '%')) ORDER BY c.codServico ASC")
    Page<CodigoServicoMegaEntity> findByDescServicoContainingIgnoreCase(@Param("descricao") String descricao, Pageable pageable);
    
    @Query("SELECT c FROM CodigoServicoMegaEntity c WHERE CAST(c.codServico AS string) LIKE CONCAT('%', :codigo, '%') ORDER BY c.codServico ASC")
    Page<CodigoServicoMegaEntity> findByCodServicoContaining(@Param("codigo") String codigo, Pageable pageable);
    
    @Query("SELECT c FROM CodigoServicoMegaEntity c WHERE " +
           "LOWER(c.descServico) LIKE LOWER(CONCAT('%', :descricao, '%')) AND " +
           "CAST(c.codServico AS string) LIKE CONCAT('%', :codigo, '%') " +
           "ORDER BY c.codServico ASC")
    Page<CodigoServicoMegaEntity> findByDescServicoAndCodServicoContaining(
            @Param("descricao") String descricao, 
            @Param("codigo") String codigo, 
            Pageable pageable);
}

