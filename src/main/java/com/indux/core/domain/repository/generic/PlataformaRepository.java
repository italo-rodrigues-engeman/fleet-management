package com.indux.core.domain.repository.generic;

import com.indux.core.domain.model.generic.Plataforma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlataformaRepository extends JpaRepository<Plataforma, Long> {
    
    /**
     * Busca todas as plataformas únicas ordenadas por nome
     */
    @Query("SELECT DISTINCT p FROM Plataforma p ORDER BY p.nomePlataforma")
    List<Plataforma> findAllDistinctOrderByNomePlataforma();
    
    /**
     * Busca apenas os nomes únicos das plataformas
     */
    @Query("SELECT DISTINCT p.nomePlataforma FROM Plataforma p ORDER BY p.nomePlataforma")
    List<String> findDistinctNomePlataforma();
    
    /**
     * Busca apenas os nomes únicos das plataformas que contenham o texto especificado
     */
    @Query("SELECT DISTINCT p.nomePlataforma FROM Plataforma p WHERE LOWER(p.nomePlataforma) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY p.nomePlataforma")
    List<String> findDistinctNomePlataformaContaining(@Param("nome") String nome);
    
    /**
     * Busca plataforma por nome
     */
    Plataforma findByNomePlataforma(String nomePlataforma);
    
    /**
     * Busca plataformas que contenham o nome especificado
     */
    List<Plataforma> findByNomePlataformaContainingIgnoreCase(String nomePlataforma);
} 