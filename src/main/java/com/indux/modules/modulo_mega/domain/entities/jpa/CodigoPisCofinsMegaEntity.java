package com.indux.modules.modulo_mega.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_codigo_pis_cofins_mega")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodigoPisCofinsMegaEntity {
    
    @Id
    @Column(name = "cod_pis_cofins")
    private Long codPisCofins;
    
    @Column(name = "desc_pis_cofins")
    private String descPisCofins;
}

