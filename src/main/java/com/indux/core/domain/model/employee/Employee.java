package com.indux.core.domain.model.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Table(name = "tb_funcionarios")
@Entity
public class Employee {
    @Id
    private UUID id;
    @Column(name = "matricula")
    private String registration;
    @Column(name = "cpf")
    private String cpf;
    @Column(name = "nome")
    private String name;
    @Column(name = "telefone")
    private String cellphone;
    @Column(name = "telefone2")
    private String cellphone2;
    @Column(name = "cargo_id") //idHcm
    private String position;
    @Column(name = "filial_id") //filialMEGA
    private Long branch_id;
    @Column(name = "filial_id_hcm")
    private Integer filialIdHcm;
    @Column(name = "situacao")
    private String statusEmployee;
    @Column(name = "rateio_id")
    private Integer contract_id;
    @Column(name = "email_particular")
    private String personalEmail;
    @Column(name = "email_comercial")
    private String businessEmail;
    @Column(name = "centro_custos_id")
    private String costCenterId;
    @Column(name = "data_demissao")
    private LocalDate terminationDate;
    @Column(name = "data_admissao")
    private LocalDate admissionDate;
    @Column(name = "data_nascimento")
    private LocalDate birthDate;
    @Column(name = "cidade_funcionario")
    private String city;
    @Column(name = "grau_instrucao")
    private String educationLevel;
    @Column(name = "sexo_colaborador")
    private String gender;
    @Column(name = "nome_mae")
    private String motherName;
    @Column(name = "nome_pai")
    private String fatherName;
    @Column(name = "estado")
    private String state;
    @Column(name = "sispat")
    private String SISPAT;
    @Column(name = "qtd_dependentes")
    private Integer numberOfDependents;
    private Boolean pj;
    @Column(name = "estado_civil")
    private String maritalStatus;
}
