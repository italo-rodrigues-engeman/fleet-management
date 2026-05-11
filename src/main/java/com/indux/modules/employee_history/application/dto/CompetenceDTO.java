package com.indux.modules.employee_history.application.dto;

import com.indux.modules.organization_chart.application.dtos.ProjectDTO;

import java.time.LocalDate;

public record CompetenceDTO(
        LocalDate competence,
        ProjectDTO project
) {

}
