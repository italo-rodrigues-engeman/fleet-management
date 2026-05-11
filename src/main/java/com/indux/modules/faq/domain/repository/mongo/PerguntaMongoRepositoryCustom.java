package com.indux.modules.faq.domain.repository.mongo;

import com.indux.modules.faq.application.dto.PerguntaFiltrosDTO;
import com.indux.modules.faq.application.dto.PerguntaTemaFiltrosDTO;
import com.indux.modules.faq.domain.entities.mongo.PerguntaMongo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PerguntaMongoRepositoryCustom {
    
    /**
     * Busca perguntas com filtros dinâmicos usando Criteria API
     */
    Page<PerguntaMongo> findWithFilters(Pageable pageable, PerguntaFiltrosDTO filtros);

    /**
     * Busca perguntas por tema com filtros dinâmicos usando Criteria API
     */
    Page<PerguntaMongo> findWithFiltersByTema(Long temaId, Pageable pageable, PerguntaTemaFiltrosDTO filtros);
}
