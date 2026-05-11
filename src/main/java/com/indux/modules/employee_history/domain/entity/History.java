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

@Table(name = "tb_historico_colaborador")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class History {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "competencia")
    private Date competence;
    @Column(name = "id_filial")
    private Long filialId;
    @Column(name = "matricula")
    private String registration;
    @Column(name = "data_inicio")
    private Date startDate;
    @Column(name = "data_fim")
    private Date endDate;
    @Column(name = "observacao")
    private String observation;
    @Column(name = "evento")
    private String event;
    @Column(name = "desc_evento")
    private String description;
}
