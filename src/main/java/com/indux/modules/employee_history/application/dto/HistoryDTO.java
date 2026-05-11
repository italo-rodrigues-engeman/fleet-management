package com.indux.modules.employee_history.application.dto;

import com.indux.core.application.dto.generic.EmployeeSummaryDTO;

import java.util.Date;
import java.util.UUID;

public record HistoryDTO(
        String id,
        Date competence,
        Long filialId,
        String filialName,
        String registration,
        UUID employeeId,
        String employeeName,
        Date startDate,
        Date endDate,
        String observation,
        String event,
        String description,
        EmployeeSummaryDTO.HierarchyInfo hierarchyInfo
) {
}
