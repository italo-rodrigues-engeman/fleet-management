package com.indux.modules.faq.domain.repository;

import com.indux.modules.faq.domain.entities.DatabaseSequenceFaqPergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseSequenceFaqPerguntaRepository extends JpaRepository<DatabaseSequenceFaqPergunta, Long> {
}


