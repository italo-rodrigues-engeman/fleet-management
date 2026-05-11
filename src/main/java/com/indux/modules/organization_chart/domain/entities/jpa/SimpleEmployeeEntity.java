package com.indux.modules.organization_chart.domain.entities.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="tb_funcionarios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleEmployeeEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "nome")
    private String name;

    @Column(name = "matricula")
    private String registration;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "centro_custos_id")
    private String costCenterId;

    @Column(name = "rateio_id")
    private Integer rateioId;

    @Column(name = "filial_id")
    private Long branchId;

    @Column(name = "filial_id_hcm")
    private Integer filialIdHcm;

    @Column(name = "situacao")
    private String status;

    @Column(name = "cargo_id")
    private String positionId;

    @Column(name = "qtd_dependentes")
    private Integer numberOfDependents;

    @Column(name = "grau_instrucao")
    private String educationLevel;

    @Column(name = "email_particular")
    private String personalEmail;

    @Column(name = "telefone")
    private String phone;

    @Column(name = "telefone2")
    private String phone2;

    @Column(name = "data_admissao")
    private LocalDate admissionDate;

    @Column(name = "data_demissao")
    private LocalDate terminationDate;

    @Column(name = "sispat")
    private String sispat;

    @Column(name = "estado")
    private String state;

    @Column(name = "cidade_funcionario")
    private String city;

    @Column(name = "sexo_colaborador")
    private String gender;

    @Column(name = "nome_mae")
    private String motherName;

    @Column(name = "nome_pai")
    private String fatherName;

    @Column(name = "data_nascimento")
    private LocalDate birthDate;
}
