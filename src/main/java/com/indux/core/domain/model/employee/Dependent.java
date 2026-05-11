package com.indux.core.domain.model.employee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_dependentes")
@IdClass(Dependent.DependentPK.class)
public class Dependent {
    @Id
    @Column(name = "cpf")
    private String cpf;

    @Id
    @Column(name = "tipo_plano")
    private String typePlan;

    @Column(name = "matricula_funcionario")
    private String employeeRegistration;

    @Column(name = "nome")
    private String name;
    
    @Column(name = "grau_parentesco")
    private String degreeKinship;
    
    @Column(name = "data_nascimento")
    private LocalDate dateBirth;
    
    @Column(name = "tipo_sexo")
    private String sexType;
    
    @Column(name = "funcionario")
    private String employee;
    
    @Column(name = "tipo_funcionario")
    private String typeEmployee;
    
    @Column(name = "codigo_plano")
    private Integer idPlan;
    
    @Column(name = "plano")
    private String plan;
    
    @Column(name = "codigo_seguradora")
    private Integer insuranceCode;
    
    @Column(name = "nome_seguradora")
    private String nameInsurance;
    
    @Column(name = "mes_inclusao")
    private String inclusionMonth;
    
    @Column(name = "mes_exclusao")
    private String exclusionMonth;
    
    @Column(name = "rateio_id")
    private Integer rateioId;
    
    @Column(name = "filial_id")
    private Integer branchId;
    
    @Column(name = "carteirinha")
    private String membershipCard;

    @Column(name = "nome_mae")
    private String motherName;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DependentPK implements Serializable {
        private String cpf;
        private String typePlan;
    }
} 