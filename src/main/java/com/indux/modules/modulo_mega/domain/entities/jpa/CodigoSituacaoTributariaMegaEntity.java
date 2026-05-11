package com.indux.modules.modulo_mega.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_codigo_situacao_tributaria_mega")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodigoSituacaoTributariaMegaEntity {
    
    @Id
    @Column(name = "cod_sit_tributaria")
    private Long codSitTributaria;
    
    @Column(name = "desc_situacao_tributaria")
    private String descSituacaoTributaria;
}

