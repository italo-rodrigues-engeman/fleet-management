package com.indux.modules.employee_history.application.dto;

import com.indux.core.application.dto.generic.EmployeeSummaryDTO;

import java.util.Date;
import java.util.UUID;

public record DaysWordedDTO(
        String id,
        Date competence,
        Long filialId,
        String filialName,
        String registration,
        UUID employeeId,
        String employeeName,
        String cargoName,
        String situation,
        String movimentation,
        Integer totalDaysWorked,
        Integer totalMonthsWorked,
        Double percDaysWorked,
        EmployeeSummaryDTO.HierarchyInfo hierarchy
) {
}

