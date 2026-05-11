package com.indux.modules.faq.domain.repository;

import com.indux.modules.faq.domain.entities.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, Long> {
    
    Optional<Resposta> findByNome(String nome);
    
    List<Resposta> findByPerguntaId(Long perguntaId);
    
    List<Resposta> findByContrato(Integer contrato);
    
    List<Resposta> findByStatus(Boolean status);
    
    @Query("SELECT r FROM Resposta r WHERE r.nome LIKE %:nome%")
    List<Resposta> findByNomeContaining(@Param("nome") String nome);
    
    @Query("SELECT r FROM Resposta r WHERE r.pergunta.id = :perguntaId AND r.nome LIKE %:nome%")
    List<Resposta> findByPerguntaIdAndNomeContaining(@Param("perguntaId") Long perguntaId, @Param("nome") String nome);
    
    @Query("SELECT r FROM Resposta r WHERE r.contrato = :contrato AND r.status = :status")
    List<Resposta> findByContratoAndStatus(@Param("contrato") Integer contrato, @Param("status") Boolean status);
    
    boolean existsByNome(String nome);
} 