package com.indux.modules.cdi.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="tb_cdi_points")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PointsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "maior_ponto", nullable = false)
    private Integer highPoint;

    @Column(name = "medio_ponto", nullable = false)
    private Integer medioPoint;

    @Column(name = "baixo_ponto", nullable = false)
    private Integer lowPoint;
}
