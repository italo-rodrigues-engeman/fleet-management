package com.indux.core.application.dto.generic;

import com.indux.core.domain.model.employee.Dependent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePlanDTO {
    private String matriculaFuncionario;
    private String funcionario;
    private String tipoFuncionario;
    private String dependenteId;
    private String nome;
    private String grauParentesco;
    private String cpf;
    private String dataNascimento;
    private String tipoSexo;
    private Integer codigoPlano;
    private String plano;
    private Integer codigoSeguradora;
    private String nomeSeguradora;
    private String mesInclusao;
    private String mesExclusao;
    private Integer rateioId;
    private Integer filialId;
    private String tipoPlano;
    private String carteirinha;
    private String nomeMae;

    public static EmployeePlanDTO fromEntity(Dependent dependent) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        return EmployeePlanDTO.builder()
                .matriculaFuncionario(dependent.getEmployeeRegistration())
                .funcionario(dependent.getEmployee())
                .tipoFuncionario(dependent.getTypeEmployee())
                .dependenteId(dependent.getCpf())
                .nome(dependent.getName())
                .grauParentesco(dependent.getDegreeKinship())
                .cpf(dependent.getCpf())
                .dataNascimento(dependent.getDateBirth() != null ? dependent.getDateBirth().format(dateFormatter) : null)
                .tipoSexo(dependent.getSexType())
                .codigoPlano(dependent.getIdPlan())
                .plano(dependent.getPlan())
                .codigoSeguradora(dependent.getInsuranceCode())
                .nomeSeguradora(dependent.getNameInsurance())
                .mesInclusao(dependent.getInclusionMonth())
                .mesExclusao(dependent.getExclusionMonth())
                .rateioId(dependent.getRateioId())
                .filialId(dependent.getBranchId())
                .tipoPlano(dependent.getTypePlan())
                .carteirinha(dependent.getMembershipCard())
                .nomeMae(dependent.getMotherName())
                .build();
    }
} 