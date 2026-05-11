package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.MeasurementPeriodResponse;
import com.indux.modules.ppu.domain.entities.item.MeasurementPeriod;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MeasurementPeriodResponseMapper {
    MeasurementPeriodResponse toResponse(MeasurementPeriod entity);
}

