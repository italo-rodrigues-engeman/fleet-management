package com.indux.modules.organization_chart.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="tb_organograma_contrato")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleContractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false)
    private String name;

    @Column(name = "os", nullable = false)
    private String os;

    @Column(name = "subordinado", nullable = false)
    private Long subordinateId;
}
