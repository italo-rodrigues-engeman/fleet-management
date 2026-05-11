package com.indux.modules.employee_history.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "tb_dias_trabalhados")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DaysWorked {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "competencia")
    private LocalDate competence;
    @Column(name = "id_filial")
    private Long filialId;
    @Column(name = "matricula")
    private String registration;
    @Column(name = "situacao")
    private String situation;
    @Column(name = "movimentacao")
    private String movimentation;
    @Column(name = "total_dias_trabalhados")
    private Integer totalDaysWorked;
    @Column(name = "total_dias_mes")
    private Integer totalMonthsWorked;
    @Column(name = "perc_dias_trabalhados")
    private Double percDaysWorked;

}
