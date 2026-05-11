package com.indux.modules.alpar.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Immutable
@Table(name = "vw_funcionarios_com_dependentes")
public class EmployeeWithDependentsView {

    @Id
    @Column(name = "matricula")
    private String registration;

    @Column(name = "nome")
    private String name;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "sexo")
    private String sex;

    @Column(name = "data_nascimento")
    private LocalDate birthDate;

    @Column(name = "nome_mae")
    private String motherName;

    @Column(name = "estado_civil")
    private String maritalStatus;

    @Column(name = "situacao")
    private String situation;

    @Column(name = "data_admissao")
    private LocalDate admissionDate;

    @Column(name = "data_demissao")
    private LocalDate dismissalDate;

    @Column(name = "dependentes", columnDefinition = "jsonb")
    private String dependentsJson;
}
