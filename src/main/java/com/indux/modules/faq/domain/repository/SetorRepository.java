package com.indux.modules.faq.domain.repository;

import com.indux.modules.faq.domain.entities.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SetorRepository extends JpaRepository<Setor, Long> {
    
    Optional<Setor> findByNome(String nome);
    
    List<Setor> findByStatus(Boolean status);
    
    @Query("SELECT s FROM Setor s WHERE s.nome LIKE %:nome%")
    List<Setor> findByNomeContaining(@Param("nome") String nome);
    
    boolean existsByNome(String nome);
} 