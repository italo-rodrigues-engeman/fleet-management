package com.indux.modules.employee_history.infra.mapper;

import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.modules.employee_history.application.dto.HistoryDTO;
import com.indux.modules.employee_history.domain.entity.History;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface HistoryMapper {
    List<HistoryDTO> toDTO(List<History> history);

    HistoryDTO toSingleDTO(History history, String filialName, UUID employeeId, String employeeName,
            EmployeeSummaryDTO.HierarchyInfo hierarchyInfo);
}
