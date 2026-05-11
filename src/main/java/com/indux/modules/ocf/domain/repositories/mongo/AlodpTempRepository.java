package com.indux.modules.ocf.domain.repositories.mongo;

import com.indux.modules.ocf.domain.entities.mongo.AlodpTempEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlodpTempRepository extends MongoRepository<AlodpTempEntity, String> {
    
    /**
     * Busca logs por CPF do colaborador
     * @param cpf CPF do colaborador
     * @return Lista de logs encontrados
     */
    List<AlodpTempEntity> findByCpf(String cpf);
    
    /**
     * Deleta logs por CPF do colaborador
     * @param cpf CPF do colaborador
     * @return Número de registros deletados
     */
    long deleteByCpf(String cpf);
}
