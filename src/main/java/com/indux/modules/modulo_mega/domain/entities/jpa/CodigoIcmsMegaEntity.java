package com.indux.modules.modulo_mega.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_codigo_icms_mega")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodigoIcmsMegaEntity {
    
    @Id
    @Column(name = "cod_icms")
    private Long codIcms;
    
    @Column(name = "desc_icms")
    private String descIcms;
}

