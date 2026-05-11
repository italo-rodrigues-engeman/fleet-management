package com.indux.modules.modulo_mega.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_codigo_servico_mega")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodigoServicoMegaEntity {
    
    @Id
    @Column(name = "cod_servico")
    private Long codServico;
    
    @Column(name = "desc_servico")
    private String descServico;
}

