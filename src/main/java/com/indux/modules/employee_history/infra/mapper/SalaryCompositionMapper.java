package com.indux.modules.employee_history.infra.mapper;

import com.indux.modules.employee_history.application.dto.SalaryCompositionResponse;
import com.indux.modules.employee_history.domain.entity.SalaryCompositionEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SalaryCompositionMapper {
    SalaryCompositionResponse toResponse(SalaryCompositionEntity entity);

    List<SalaryCompositionResponse> toResponseList(List<SalaryCompositionEntity> entities);
}
