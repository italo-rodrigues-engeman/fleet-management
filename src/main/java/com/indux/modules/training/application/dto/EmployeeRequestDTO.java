package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;

public record EmployeeRequestDTO(
        String id,
        @JsonProperty("nome")
        String name,
        @JsonProperty("matricula")
        String registration,
        @JsonProperty("cargo")
        String position,
        @JsonProperty("situacao")
        String situation,
        @JsonProperty("nota")
        String score,
        @JsonProperty("anexoCriar")
        MultipartFile file
) {
}
