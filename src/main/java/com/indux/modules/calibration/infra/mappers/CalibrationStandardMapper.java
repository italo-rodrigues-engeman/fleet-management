package com.indux.modules.calibration.infra.mappers;

import com.indux.modules.calibration.aplication.dtos.CalibrationStandardAll;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardCreate;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardReturn;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationEntity;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RangeMapper.class})
public interface CalibrationStandardMapper {
    List<CalibrationStandardAll> toDTOAll(List<CalibrationStandardEntity> calibrationStandardAll);
    CalibrationStandardEntity toEntity(CalibrationStandardCreate createDto);
    CalibrationStandardReturn toDTOReturn(CalibrationStandardEntity entity);
}