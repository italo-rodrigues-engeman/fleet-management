package com.indux.modules.employee_history.application.dto;

import java.time.LocalDate;

/**
 * Projeção intermediária retornada pela query de competências agrupadas por projeto/mês.
 * O projectId é usado pelo service para buscar o ProjectDTO completo via
 * organizationProjectRepository.findByIdWithDetails() — sem lookup intermediário por filial.
 */
public record CompetenceFilialProjection(
        LocalDate competence,
        Long projectId) {
}
