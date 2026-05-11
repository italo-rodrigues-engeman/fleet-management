package com.indux.modules.modulo_mega.domain.repository;

import com.indux.modules.modulo_mega.domain.persistence.view.MegaEntityOrganogram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface MegaEntityOrganogramRepository extends JpaRepository<MegaEntityOrganogram, Integer> {

    // O método findAll() já existe por padrão no JpaRepository, 
    // então não precisamos declará-lo explicitamente aqui.
    
  
}