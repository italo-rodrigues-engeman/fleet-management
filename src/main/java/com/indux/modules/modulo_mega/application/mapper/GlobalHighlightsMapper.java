package com.indux.modules.modulo_mega.application.mapper;

import com.indux.modules.modulo_mega.application.dto.RegionalDetailDTO;
import com.indux.modules.modulo_mega.application.dto.ItemDetailedDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GlobalHighlightsMapper {
    
    // =================================================================================
    // Mapeamento de Preços (Source: Estrutura Aninhada em RegionalDetailDTO/BaseStatisticsDTO)
    // Versão Simplificada
    // =================================================================================

    // Minimum Price Mappings
    @Mapping(source = "priceAnalysis.minimumPrice", target = "priceAnalysis.minimumPrice") // Mapeia sub-objeto (MapStruct mapeia automaticamente propriedades com nomes iguais)
    @Mapping(source = "priceAnalysis.minimumPrice.alterationUser", target = "priceAnalysis.minimumPrice.editionUser") // Sobrescreve apenas a propriedade com nome diferente

    // Maximum Price Mappings
    @Mapping(source = "priceAnalysis.maximumPrice", target = "priceAnalysis.maximumPrice") // Mapeia sub-objeto
    @Mapping(source = "priceAnalysis.maximumPrice.alterationUser", target = "priceAnalysis.maximumPrice.editionUser") // Sobrescreve apenas a propriedade com nome diferente

    // Average Price Mappings
    @Mapping(source = "priceAnalysis.averagePrice", target = "priceAnalysis.averagePrice") // Mapeia sub-objeto
    @Mapping(source = "priceAnalysis.averagePrice.alterationUser", target = "priceAnalysis.averagePrice.editionUser") // Sobrescreve apenas a propriedade com nome diferente
    
    void fillGlobalHighlights(@MappingTarget ItemDetailedDTO dto, RegionalDetailDTO stats);
}