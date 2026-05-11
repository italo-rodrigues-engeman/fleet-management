package com.indux.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependentDTO {
    private String cpf;
    private String typePlan;
    private String employeeRegistration;
    private String name;
    private String degreeKinship;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateBirth;
    
    private String sexType;
    private String employee;
    private String typeEmployee;
    private Integer idPlan;
    private String plan;
    private Integer insuranceCode;
    private String nameInsurance;
    private String inclusionMonth;
    private String exclusionMonth;
    private Integer rateioId;
    private Integer branchId;
    private String membershipCard;
} 