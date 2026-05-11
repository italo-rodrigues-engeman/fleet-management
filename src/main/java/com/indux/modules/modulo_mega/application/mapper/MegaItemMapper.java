package com.indux.modules.modulo_mega.application.mapper;

import com.indux.modules.modulo_mega.application.dto.items.MegaItemResponse;
import com.indux.modules.modulo_mega.domain.entities.jpa.MegaItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MegaItemMapper {

    MegaItemResponse toResponse(MegaItem item);
    List<MegaItemResponse> toResponse(List<MegaItem> itens);

}
