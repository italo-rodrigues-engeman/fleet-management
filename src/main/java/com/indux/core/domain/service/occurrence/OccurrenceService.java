package com.indux.core.domain.service.occurrence;

import com.indux.core.application.dto.generic.BatchStatusDTO;
import com.indux.core.application.dto.generic.GenericMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Interface que define as operações de gerenciamento de ocorrências,
 * incluindo criação, transições de etapas, revisões, finalizações,
 * consultas paginadas e atualização em lote de status.
 * @param <T> tipo da entidade de ocorrência (por exemplo, OcorrenciaFF)
 */
public interface OccurrenceService<T> {
    /**
     * Cria uma nova ocorrência com base nos dados do DTO fornecido.
     * @param dto    objeto de transferência contendo dados iniciais da ocorrência
     * @param userId identificador do usuário que solicita a criação
     * @return mensagem genérica com status e código de resposta HTTP
     * @throws InterruptedException se a geração de sequência ou outra operação assíncrona falhar
     */
    GenericMessage createOccurrence(Object dto, String userId) throws InterruptedException;

    /**
     * Avança a ocorrência para a próxima etapa do fluxo.
     * @param occurrenceId identificador da ocorrência a ser atualizada
     * @param dto          objeto de transferência com dados auxiliares (pode ser null)
     * @param userId       identificador do usuário que realiza a operação
     * @return mensagem genérica com status da operação
     */
    GenericMessage moveToNextStep(String occurrenceId, Object dto, String userId);

    /**
     * Solicita revisão de uma ocorrência em uma etapa anterior.
     * @param occurrenceId identificador da ocorrência para revisão
     * @param dto          DTO contendo observações ou dados de solicitação
     * @param userId       identificador do usuário que solicita a revisão
     * @return mensagem genérica indicando sucesso ou falha
     */
    GenericMessage requestReview(String occurrenceId, Object dto, String userId);

    /**
     * Edita uma ocorrência após revisão, podendo distinguir entre edição simples e revisão final.
     * @param occurrenceId identificador da ocorrência a ser editada
     * @param dto          DTO com dados atualizados
     * @param userId       identificador do usuário que está editando
     * @param isEdit       flag que indica se é uma edição (true) ou finalização de revisão (false)
     * @return mensagem genérica com resultado da operação
     */
    GenericMessage editAfterReview(String occurrenceId, Object dto, String userId, boolean isEdit);

    /**
     * Rejeita uma ocorrência, encerrando-a no status REJEITADO.
     * @param occurrenceId identificador da ocorrência a rejeitar
     * @param dto          DTO opcional contendo motivo da rejeição
     * @param userId       identificador do usuário que rejeita a ocorrência
     * @return mensagem genérica confirmando a rejeição
     */
    GenericMessage rejectOccurrence(String occurrenceId, Object dto, String userId);

    /**
     * Finaliza uma ocorrência, marcando-a como aprovada e encerrada.
     * @param occurrenceId identificador da ocorrência a finalizar
     * @param dto          DTO contendo comprovante ou informações finais necessárias
     * @param userId       identificador do usuário que finaliza a ocorrência
     * @return mensagem genérica confirmando a finalização
     */
    GenericMessage finalizeOccurrence(String occurrenceId, Object dto, String userId);

    /**
     * Recupera uma ocorrência pelo seu identificador.
     * @param id identificador da ocorrência
     * @return instância da ocorrência, ou lança exceção se não encontrada
     */
    T getOccurrence(String id);

    /**
     * Lista ocorrências criadas por um determinado usuário.
     * @param userId   UUID do usuário solicitante
     * @param pageable parâmetros de paginação e ordenação
     * @return página de ocorrências correspondentes
     */
    Page<T> listOccurrencesByUser(UUID userId, Pageable pageable);

    /**
     * Lista ocorrências às quais o usuário tem permissão de acesso, considerando seus módulos e permissões configurados.
     * @param userId   UUID do usuário
     * @param pageable parâmetros de paginação e ordenação
     * @return página de ocorrências permitidas
     */
    Page<T> listOccurrencesAllowed(UUID userId, Pageable pageable);

    /**
     * Lista todas as ocorrências, filtradas pelo escopo do usuário se aplicável.
     * @param userId   UUID do usuário
     * @param pageable parâmetros de paginação e ordenação
     * @return página de ocorrências visíveis ao usuário
     */
    Page<T> listAllAllowed(UUID userId, Pageable pageable);

    /**
     * Lista todas as ocorrências sem filtros de usuário.
     * @param pageable parâmetros de paginação e ordenação
     * @return página contendo todas as ocorrências
     */
    Page<T> listAll(Pageable pageable);

    /**
     * Pesquisa ocorrências com base em critérios dinâmicos encapsulados no filtro.
     * @param filter       DTO com critérios de busca (status, datas, etc.)
     * @param filterGlobal se true, aplica todos os filtros; se false, aplica apenas escopo de usuário
     * @param userId       identificador do usuário para definição de escopo
     * @return lista de ocorrências que satisfazem os critérios
     */
    Page<T> searchFilter(Object filter, boolean filterGlobal, String userId, Pageable pageable);

    /**
     * Atualiza em lote o status de várias ocorrências.
     * @param ids  DTO contendo lista de IDs de ocorrências e observação comum
     * @param user UUID do usuário que executa a operação em lote
     */
    void batchUpdateStatus(BatchStatusDTO ids, UUID user);

}