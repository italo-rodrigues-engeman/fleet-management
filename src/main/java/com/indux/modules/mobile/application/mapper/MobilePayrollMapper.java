package com.indux.modules.mobile.application.mapper;

import com.indux.modules.employee_history.domain.entity.PayrollEntity;
import com.indux.modules.mobile.application.dto.response.MobilePayrollEventResponse;
import com.indux.modules.mobile.application.dto.response.MobilePayrollSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.Date;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MobilePayrollMapper {

    MobilePayrollEventResponse toEvent(PayrollEntity entity);

    default MobilePayrollSummaryResponse toSummary(Date competence) {
        return MobilePayrollSummaryResponse.builder()
                .competence(competence)
                .build();
    }
}
