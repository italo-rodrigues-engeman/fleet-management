package com.indux.modules.faq.domain.repository.mongo;

import com.indux.modules.faq.domain.entities.mongo.PerguntaMongo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface PerguntaMongoRepository extends MongoRepository<PerguntaMongo, String>, PerguntaMongoRepositoryCustom {
    Optional<PerguntaMongo> findByCodigoSequencial(Long codigoSequencial);

    Page<PerguntaMongo> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    Page<PerguntaMongo> findByContratoId(Long contratoId, Pageable pageable);

    Page<PerguntaMongo> findByContratoIdAndTituloContainingIgnoreCase(Long contratoId, String titulo, Pageable pageable);

    Page<PerguntaMongo> findByRegionalId(Long regionalId, Pageable pageable);

    Page<PerguntaMongo> findByRegionalIdAndTituloContainingIgnoreCase(Long regionalId, String titulo, Pageable pageable);

    Page<PerguntaMongo> findByRegionalIdAndContratoId(Long regionalId, Long contratoId, Pageable pageable);

    Page<PerguntaMongo> findByRegionalIdAndContratoIdAndTituloContainingIgnoreCase(Long regionalId, Long contratoId, String titulo, Pageable pageable);

    Page<PerguntaMongo> findByTemaId(Long temaId, Pageable pageable);
    Page<PerguntaMongo> findByTemaIdAndTituloContainingIgnoreCase(Long temaId, String titulo, Pageable pageable);
    Page<PerguntaMongo> findByTemaIdAndContratoId(Long temaId, Long contratoId, Pageable pageable);
    Page<PerguntaMongo> findByTemaIdAndRegionalId(Long temaId, Long regionalId, Pageable pageable);
    Page<PerguntaMongo> findByTemaIdAndContratoIdAndTituloContainingIgnoreCase(Long temaId, Long contratoId, String titulo, Pageable pageable);
    Page<PerguntaMongo> findByTemaIdAndRegionalIdAndTituloContainingIgnoreCase(Long temaId, String titulo, Pageable pageable);
    Page<PerguntaMongo> findByTemaIdAndRegionalIdAndContratoId(Long temaId, Long regionalId, Long contratoId, Pageable pageable);
    Page<PerguntaMongo> findByTemaIdAndRegionalIdAndContratoIdAndTituloContainingIgnoreCase(Long temaId, Long regionalId, Long contratoId, String titulo, Pageable pageable);

    List<PerguntaMongo> findByTemaId(Long temaId);
    
    // ========== MÉTODOS DE FILTRO AVANÇADO ==========
    // NOTA: Os filtros agora são implementados usando Criteria API no service
    // para maior flexibilidade e performance
    
    // Método auxiliar para encontrar o maior contador de step log de uma pergunta específica
    default Optional<Integer> findMaxStepCounterByPerguntaId(String perguntaId) {
        return findById(perguntaId)
                .map(pergunta -> pergunta.getStepLog() != null ? 
                    pergunta.getStepLog().stream()
                        .map(PerguntaMongo.StepLogEmbedded::getStepCounter)
                        .filter(Objects::nonNull)
                        .max(Comparator.naturalOrder())
                        .orElse(0) : 0);
    }
    

}


