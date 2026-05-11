package com.indux.modules.alpar.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.alpar.domain.Dependent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeWithDependentsResponse {

    @JsonProperty("matricula")
    private String registration;

    @JsonProperty("nome")
    private String name;

    @JsonProperty("cpf")
    private String cpf;

    @JsonProperty("sexo")
    private String sex;

    @JsonProperty("data_nascimento")
    private LocalDate birthDate;

    @JsonProperty("nome_mae")
    private String motherName;

    @JsonProperty("estado_civil")
    private String maritalStatus;

    @JsonProperty("situacao")
    private String situation;

    @JsonProperty("data_admissao")
    private LocalDate admissionDate;

    @JsonProperty("data_demissao")
    private LocalDate dismissalDate;

    @JsonProperty("dependentes")
    private List<Dependent> dependents;
}
