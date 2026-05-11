package com.indux.modules.budgets.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleBudgetItemGroupResponseDTO {
    private String id;
    private Integer sequentialId;
    private String name;
    private String description;
}
