package com.indux.core.domain.model.employee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_plataformas")
public class Platform {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "nome_plataforma")
    private String nomePlataforma;

    @Column(name = "sigla")
    private String sigla;

    @Column(name = "apelido")
    private String apelido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kogni_contrato", referencedColumnName = "id")
    private ContractProject contract;
} 