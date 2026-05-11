package com.indux.core.domain.service.occurrence.log;

import com.indux.core.domain.model.modules.form.LogFilter;
import com.indux.core.domain.model.modules.form.StepLog;

import java.util.List;

/**
 * Interface base para serviços de gerenciamento de logs de etapas (StepLog).
 * @param <T> Classe que estende {@link StepLog}
 */
public interface OccurrenceLogService<T extends StepLog> {
    /**
     * Deleta um log específico.
     * @param object Log que será deletado.
     */
    void delete(T object);

    /**
     * Salva ou atualiza um log no banco de dados.
     * @param object Log que será salvo ou atualizado.
     */
    void save(T object);

    /**
     * Retorna todos os logs associados a uma ocorrência específica.
     * @param occurrenceId ID da ocorrência.
     * @return Lista de logs ordenados por data de criação.
     */
    List<T> findByOccurrenceID(String occurrenceId);

    /**
     * Filtra os logs com base em critérios específicos definidos no filtro.
     * @param filter Objeto de filtro específico para logs.
     * @return Lista de logs que atendem aos critérios.
     */
    List<T> filter(LogFilter filter);
}
