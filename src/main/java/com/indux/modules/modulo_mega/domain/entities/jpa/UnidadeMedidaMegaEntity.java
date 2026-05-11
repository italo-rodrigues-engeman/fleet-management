package com.indux.modules.modulo_mega.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_unidade_medida_mega")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnidadeMedidaMegaEntity {
    
    @Id
    @Column(name = "uni_med_item")
    private String uniMedItem;
    
    @Column(name = "desc_med_item")
    private String descMedItem;
}
