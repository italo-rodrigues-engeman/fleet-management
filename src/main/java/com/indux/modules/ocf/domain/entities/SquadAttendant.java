package com.indux.modules.ocf.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(
        name = "tb_times_atendentes"
)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquadAttendant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time_id", nullable = false)
    private Long squadId;

    @Column(name = "atendente_id")
    private Long attendantId;
}
