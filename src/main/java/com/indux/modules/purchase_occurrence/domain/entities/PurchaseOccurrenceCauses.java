package com.indux.modules.purchase_occurrence.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_ocorrencia_compras_causas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOccurrenceCauses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;
}

