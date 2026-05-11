package com.indux.core.domain.model.employee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "tb_coordenadores_contrato")
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ContractCoordinator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String name;

    @Column(name = "id_contrato_kogni")
    private Integer contractId;

    @Column(name = "tipo_coordenador")
    private String coordinatorType;

    @Column(name = "email")
    private String email;

    @Column(name = "contato")
    private String contact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato_kogni", referencedColumnName = "id", insertable = false, updatable = false)
    private ContractProject contract;
} 