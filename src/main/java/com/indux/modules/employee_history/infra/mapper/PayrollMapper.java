package com.indux.modules.employee_history.infra.mapper;

import com.indux.modules.employee_history.application.dto.PayrollResponse;
import com.indux.modules.employee_history.domain.entity.PayrollEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PayrollMapper {
    PayrollResponse toResponse(PayrollEntity entity);

    List<PayrollResponse> toResponseList(List<PayrollEntity> entities);
}
