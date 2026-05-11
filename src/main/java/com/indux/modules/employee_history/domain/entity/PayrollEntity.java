package com.indux.modules.employee_history.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Table(name = "tb_folha_pagamento")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayrollEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "competencia")
    private Date competence;
    @Column(name = "filial_hcm")
    private Long filialHcm;
    @Column(name = "matricula")
    private String registration;
    @Column(name = "data_pagamento")
    private Date paymentDate;
    @Column(name = "nome_evento")
    private String eventName;
    @Column(name = "desc_tipo_evento")
    private String eventDescription;
    @Column(name = "valor")
    private Double value;
}
