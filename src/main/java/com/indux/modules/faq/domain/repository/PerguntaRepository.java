package com.indux.modules.faq.domain.repository;

import com.indux.modules.faq.domain.entities.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {
    
    Optional<Pergunta> findByNome(String nome);
    
    List<Pergunta> findByTemaId(Long temaId);
    
    @Query("SELECT p FROM Pergunta p WHERE p.nome LIKE %:nome%")
    List<Pergunta> findByNomeContaining(@Param("nome") String nome);
    
    @Query("SELECT p FROM Pergunta p WHERE p.tema.id = :temaId AND p.nome LIKE %:nome%")
    List<Pergunta> findByTemaIdAndNomeContaining(@Param("temaId") Long temaId, @Param("nome") String nome);
    
    boolean existsByNome(String nome);
} 