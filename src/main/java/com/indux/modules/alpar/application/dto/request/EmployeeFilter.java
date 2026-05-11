package com.indux.modules.alpar.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EmployeeFilter {

    @JsonProperty("cpf")
    private String cpf;

    @JsonProperty("data_nascimento")
    private LocalDate birthDate;
}
