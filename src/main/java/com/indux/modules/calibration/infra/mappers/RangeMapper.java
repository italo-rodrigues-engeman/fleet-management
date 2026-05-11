package com.indux.modules.calibration.infra.mappers;

import com.indux.modules.calibration.aplication.dtos.RangeCreate;
import com.indux.modules.calibration.domain.entities.mongo.RangeEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RangeMapper {
    RangeEntity toEntity(RangeCreate createDto);
    List<RangeEntity> toEntityList(List<RangeCreate> createDtoList);
}