package com.indux.core.infra.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MergeUpdate {
    private MergeUpdate() { /* utilitário */ }

    /**
     * Faz merge entre uma lista existente e uma lista de DTOs:
     * - Para cada DTO, se existir item na lista antiga com mesma chave, atualiza via updateExisting.
     * - Caso contrário, cria um novo item via createNew.
     * - Se keepOld == true, mantém também os itens antigos não presentes nos DTOs.
     * @param existing       lista atual de entidades
     * @param dtos           lista nova de DTOs
     * @param existingKey    extrai a chave única da entidade
     * @param dtoKey         extrai a mesma chave do DTO
     * @param updateExisting recebe (entidade, dto) para atualizar campos
     * @param createNew      recebe dto e retorna nova entidade
     * @param keepOld        se true, itens antigos não mencionados no DTO são mantidos no resultado
     * @return nova lista com o merge
     */
    public static <E, D, K> List<E> merge(
            List<E> existing,
            List<D> dtos,
            Function<? super E, K> existingKey,
            Function<? super D, K> dtoKey,
            BiConsumer<? super E, ? super D> updateExisting,
            Function<? super D, ? extends E> createNew,
            boolean keepOld
    ) {
        if (dtos == null) {
            return existing == null ? List.of() : List.copyOf(existing);
        }
        Map<K, E> byKey = (existing == null ? List.<E>of() : existing).stream()
                .collect(Collectors.toMap(existingKey, Function.identity()));

        List<E> result = new ArrayList<>(dtos.size() + (keepOld ? byKey.size() : 0));
        Set<K> seen = new HashSet<>();

        for (D dto : dtos) {
            K key = dtoKey.apply(dto);
            E entity = byKey.get(key);
            if (entity != null) {
                updateExisting.accept(entity, dto);
            } else {
                entity = createNew.apply(dto);
            }
            result.add(entity);
            seen.add(key);
        }

        if (keepOld) {

            for (Map.Entry<K, E> e : byKey.entrySet()) {
                if (!seen.contains(e.getKey())) {
                    result.add(e.getValue());
                }
            }
        }

        return result;
    }

}
