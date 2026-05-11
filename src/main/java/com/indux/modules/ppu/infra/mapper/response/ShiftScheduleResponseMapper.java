package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.ShiftScheduleResponse;
import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShiftScheduleResponseMapper {
    ShiftScheduleResponse toResponse(ShiftSchedule entity);
}

