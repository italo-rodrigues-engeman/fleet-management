package com.indux.modules.modulo_mega.application.mapper;

import com.indux.modules.modulo_mega.application.dto.OrderResponseItemDTO;
import com.indux.modules.modulo_mega.application.dto.OrdersGroupedDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "idItem", target = "itemCode")
    @Mapping(source = "itemName", target = "itemDescription")
    @Mapping(source = "totalItemValue", target = "itemValue")
    // Todos os outros campos serão mapeados automaticamente por terem nomes idênticos
    OrderResponseItemDTO toItemDto(OrdersGroupedDTO row);
}