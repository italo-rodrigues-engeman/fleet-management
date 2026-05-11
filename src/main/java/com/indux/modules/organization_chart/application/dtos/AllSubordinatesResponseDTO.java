package com.indux.modules.organization_chart.application.dtos;

import java.util.List;

public record AllSubordinatesResponseDTO(
        List<SubordinatesByTypeDTO> subordinatesByType
) { }
