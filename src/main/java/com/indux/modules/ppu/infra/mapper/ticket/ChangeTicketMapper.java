package com.indux.modules.ppu.infra.mapper.ticket;

import com.indux.modules.ppu.application.dtos.requests.ChangeTicketRequest;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChangeTicketMapper {

    @Mapping(source = "itemId", target = "itemId")
    @Mapping(source = "lineType", target = "lineType")
    @Mapping(source = "platform", target = "platform")
    @Mapping(source = "quantity", target = "quantity")
    ChangeTicket.ChangeItem toEntity(ChangeTicketRequest.ChangeTicketItemRequest request);

    default ChangeTicket.ChangeItem.LineType mapLineType(String lineType) {
        if (lineType == null) return null;
        return switch (lineType.toUpperCase()) {
            case "SERVICE", "SERVICO" -> ChangeTicket.ChangeItem.LineType.SERVICE;
            case "EQUIPMENT", "EQUIPAMENTO" -> ChangeTicket.ChangeItem.LineType.EQUIPMENT;
            case "STEEL_CABLE", "CABO_ACO" -> ChangeTicket.ChangeItem.LineType.STEEL_CABLE;
            case "ACCESSORY_KIT", "KIT_ACESSORIO" -> ChangeTicket.ChangeItem.LineType.ACCESSORY_KIT;
            default -> throw new IllegalArgumentException("Tipo de linha inválido: " + lineType);
        };
    }

}
