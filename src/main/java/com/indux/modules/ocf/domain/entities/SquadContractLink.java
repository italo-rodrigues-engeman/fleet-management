package com.indux.modules.ocf.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(
        name = "tb_times_contratos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_time_projeto", columnNames = {"time_id", "projeto"})
        }
)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquadContractLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time_id", nullable = false)
    private Long timeId;

    @Column(name = "contrato")
    private Long contrato;

    @Column(name = "projeto", nullable = false)
    private Long projeto;
}
