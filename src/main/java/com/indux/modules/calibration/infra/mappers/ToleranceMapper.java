package com.indux.modules.calibration.infra.mappers;

import com.indux.modules.calibration.aplication.dtos.ToleranceCreate;
import com.indux.modules.calibration.domain.entities.mongo.ToleranceEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ToleranceMapper {
    ToleranceEntity toEntity(ToleranceCreate createDto);
    List<ToleranceEntity> toEntityList(List<ToleranceCreate> createDtoList);
}