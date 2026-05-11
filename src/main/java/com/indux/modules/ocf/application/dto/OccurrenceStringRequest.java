package com.indux.modules.ocf.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OccurrenceStringRequest(
        @NotEmpty(message = "Lista de strings não pode estar vazia")
        @Size(max = 100, message = "Máximo de 100 strings por requisição")
        List<String> strings
) {
}
