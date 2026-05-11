package com.indux.modules.employee_history.infra.mapper;

import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.modules.employee_history.application.dto.DaysWordedDTO;
import com.indux.modules.employee_history.domain.entity.DaysWorked;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DaysWorkedMapper {
    List<DaysWordedDTO> toDTO(List<DaysWorked> daysWorked);

    DaysWordedDTO toSingleDTO(DaysWorked daysWorked, String filialName, UUID employeeId, String employeeName,
            String cargoName, EmployeeSummaryDTO.HierarchyInfo hierarchy);
}

