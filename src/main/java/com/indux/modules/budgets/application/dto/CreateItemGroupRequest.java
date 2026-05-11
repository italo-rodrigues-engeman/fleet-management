package com.indux.modules.budgets.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateItemGroupRequest {
    @NotBlank(message = "O nome do grupo é obrigatório")
    private String name;
    
    private String description;
}
