package com.indux.modules.organization_chart.domain.entities.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="tb_centro_custos_hcm")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HcmEntity {
    @Id
    @Column(name = "cc_id", nullable = false)
    private Integer ccId;

    @Column(name = "rateio_id")
    private Integer rateio_id;

    @Column(name = "nome_cc")
    private String nomeCc;
}
