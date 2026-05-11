package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.lines.EquipmentLineResponse;
import com.indux.modules.ppu.domain.entities.ppu.EquipmentLine;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EquipmentEntityResponseMapper.class})
public interface EquipmentLineResponseMapper {
    EquipmentLineResponse toResponse(EquipmentLine entity);

    List<EquipmentLineResponse> toResponses(List<EquipmentLine> entities);
}

