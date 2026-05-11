package com.indux.modules.modulo_mega.application.mapper;

import com.indux.modules.modulo_mega.application.dto.AbcFilterDTO;
import com.indux.modules.modulo_mega.application.dto.AbcItemDTO;
import com.indux.modules.modulo_mega.application.dto.AbcItemDetailDTO;
import com.indux.modules.modulo_mega.domain.entity.abc.AbcFilter;
import com.indux.modules.modulo_mega.domain.entity.abc.AbcItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AbcItemMapper {

    AbcItemMapper INSTANCE = Mappers.getMapper(AbcItemMapper.class);

    AbcFilter toDomain(AbcFilterDTO dto);
    
    AbcItemDTO toDto(AbcItem domain);

    // Mapeamentos de campos com nomes diferentes entre AbcItemDTO e AbcItemDetailDTO
    
    @Mapping(target = "totalOrders", source = "orderCount")
    @Mapping(target = "averagePrice", source = " averagePriceFromSupplier")
    @Mapping(target = "totalValue", source = "totalItemValue")
    @Mapping(target = "status", source = "itemStatus")
   
    
    // Ignoramos a curva pois ela é calculada pela lógica matemática do Service
    @Mapping(target = "calculatedCurveClass", ignore = true) 
    AbcItemDetailDTO toDetailDto(AbcItemDTO dto);

    // Método para converter a lista inteira vinda do Queries
    List<AbcItemDetailDTO> toDetailDtoList(List<AbcItemDTO> dtos);
}