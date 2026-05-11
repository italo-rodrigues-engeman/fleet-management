package com.indux.core.domain.model.employee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_cargo_filial")
public class CargoFilial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cargo_id")
    private Long cargoId;

    @Column(name = "filial_id_hcm")
    private Integer filialIdHcm;
    
    @Column(name = "quantidade_funcionarios")
    private Integer quantidadeFuncionarios;
    
    // Relationship with Cargo
/*    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", insertable = false, updatable = false)
    private Cargo cargo;*/
}