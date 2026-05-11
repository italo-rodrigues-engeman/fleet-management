package com.indux.core.domain.service.generic;

import com.indux.core.application.dto.generic.RegionalDTO;
import com.indux.core.domain.model.employee.Regional;

import java.util.List;
import java.util.Optional;

public interface RegionalService {

    /**
     * Busca uma regional por ID incluindo suas filiais
     */
    Optional<Regional> findByIdWithFiliais(Long id);

    /**
     * Busca todas as regionais com suas filiais
     */
    List<Regional> findAllWithFiliais();

    /**
     * Busca regional por nome
     */
    Optional<Regional> findByRegional(String regional);

    /**
     * Busca regionais que contenham o nome especificado
     */
    List<Regional> findByRegionalContainingIgnoreCase(String regional);

    /**
     * Busca todas as regionais
     */
    List<Regional> findAll();

    /**
     * Salva uma regional
     */
    Regional save(Regional regional);

    /**
     * Deleta uma regional por ID
     */
    void deleteById(Long id);

    /**
     * Busca todas as filiais de uma regional específica
     */
    List<Regional> findFiliaisByRegionalId(Long regionalId);

    /**
     * Busca todas as filiais de uma regional por nome
     */
    List<Regional> findFiliaisByRegionalName(String regionalName);

    /**
     * Busca a primeira filial de uma regional por nome
     */
    default Optional<Long> findFirstFilialByRegionalName(String regionalName) {
        Optional<Regional> regional = findByRegional(regionalName);
        return regional.flatMap(r -> r.getFiliais().stream()
                .findFirst()
                .map(filial -> filial.getBranchId()));
    }

    /**
     * Exemplo de uso: Busca todas as filiais de uma regional específica
     * Este método demonstra como usar a relação entre Regional e Filial
     */
    default List<RegionalDTO.FilialDTO> getFiliaisByRegionalId(Long regionalId) {
        Optional<Regional> regional = findByIdWithFiliais(regionalId);
        return regional.map(r -> r.getFiliais().stream()
                .map(RegionalDTO.FilialDTO::fromEntity)
                .toList())
                .orElse(List.of());
    }
} 