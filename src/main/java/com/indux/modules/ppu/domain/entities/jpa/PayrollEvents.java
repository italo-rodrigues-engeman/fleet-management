package com.indux.modules.ppu.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "tb_eventos_base")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollEvents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_evento", nullable = false)
    private String name;

    @OneToMany(mappedBy = "payrollEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollEventVariable> eventsVariables;
}
