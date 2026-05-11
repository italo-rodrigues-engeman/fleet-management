package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.ItemReclamado;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemReclamadoRepository extends MongoRepository<ItemReclamado, String> {
    List<ItemReclamado> findByOcorrenciaId(String ocorrenciaId);
    void deleteByOcorrenciaId(String ocorrenciaId);
} 