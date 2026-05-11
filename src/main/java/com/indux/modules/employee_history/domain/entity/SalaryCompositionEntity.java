package com.indux.modules.employee_history.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Table(name = "vw_composicao_salarial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Immutable
public class SalaryCompositionEntity {
    @Id
    @Column(name = "matricula")
    private String registration;
    @Column(name = "filial_id_hcm")
    private Integer filialIdHcm;
    @Column(name = "salario")
    private Double salary;
    @Column(name = "sobre_aviso")
    private Double notice;
    @Column(name = "periculosidade")
    private Double dangerousness;
    @Column(name = "vr")
    private Double vr;
    @Column(name = "flash")
    private Double flash;
    @Column(name = "auxilio_moradia")
    private Double housingAllowance;
    @Column(name = "prem_desemp_contratual")
    private Double award;
    @Column(name = "ajuda_de_custo")
    private Double costAllowance;
    @Column(name = "premio_desempenho")
    private Double performanceAward;
    @Column(name = "adicional_noturno")
    private Double nightBonus;
    @Column(name = "ahra")
    private Double ahra;
}