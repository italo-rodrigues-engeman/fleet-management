package com.indux.modules.organization_chart.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="tb_organograma_projeto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "contrato", nullable = true)
    private Long contractId;

    @Column(name = "subordinado", nullable = true)
    private Long subordinateId;

    @Column(name = "hcm", nullable = false)
    private Integer hcmId;

    @Column(name = "ativo", nullable = true)
    private boolean ativo;
}
