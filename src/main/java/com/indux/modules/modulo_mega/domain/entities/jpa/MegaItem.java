package com.indux.modules.modulo_mega.domain.entities.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tb_itens_mega")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MegaItem {

    @Id
    @Column(name = "id_item")
    private Integer id;

    @Column(name = "item")
    private String name;

    @Column(name = "id_grupo")
    private Integer groupId;

    @Column(name = "dt_inclusao")
    private LocalDate creationDate;

    @Column(name = "usuario_inclusao")
    private String createdBy;

    @Column(name = "usuario_alteracao")
    private String updatedBy;

    @Column(name = "uni_med_item")
    private String unitOfMeasure;

    @Column(name = "status_item")
    private String status;

    @Column(name = "previsao_inativacao")
    private LocalDate inactivationForecast;
}