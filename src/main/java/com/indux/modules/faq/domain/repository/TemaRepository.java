package com.indux.modules.faq.domain.repository;

import com.indux.modules.faq.domain.entities.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {
    
    Optional<Tema> findByNome(String nome);
    
    List<Tema> findBySetorId(Long setorId);
    
    List<Tema> findBySetorIdOrderByOrdenacaoAsc(Long setorId);
    
    List<Tema> findAllByOrderByOrdenacaoAsc();
    
    List<Tema> findByNomeContainingOrderByOrdenacaoAsc(String nome);
    
    List<Tema> findBySetorIdAndNomeContainingOrderByOrdenacaoAsc(Long setorId, String nome);
    
    @Query("SELECT MAX(t.ordenacao) FROM Tema t")
    Optional<Integer> findMaxOrdenacao();
    
    @Modifying
    @Query("UPDATE Tema t SET t.ordenacao = :novaOrdem WHERE t.id = :temaId")
    void updateOrdenacao(@Param("temaId") Long temaId, @Param("novaOrdem") Integer novaOrdem);
    
    boolean existsByNome(String nome);
} 