package com.indux.modules.modulo_mega.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_codigo_aplicacao_mega")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodigoAplicacaoMegaEntity {
    
    @Id
    @Column(name = "cod_aplicacao")
    private Long codAplicacao;
    
    @Column(name = "desc_aplicacao")
    private String descAplicacao;
}

