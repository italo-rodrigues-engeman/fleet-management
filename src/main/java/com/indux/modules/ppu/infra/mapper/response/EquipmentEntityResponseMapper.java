package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.EquipmentEntityResponse;
import com.indux.modules.ppu.domain.entities.item.EquipmentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EquipmentEntityResponseMapper {
    EquipmentEntityResponse toResponse(EquipmentEntity entity);
}

