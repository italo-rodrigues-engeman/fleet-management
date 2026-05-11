package com.indux.modules.modulo_mega.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_codigo_sped_fiscal_mega")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodigoSpedFiscalMegaEntity {
    
    @Id
    @Column(name = "cod_sped_fiscal")
    private Long codSpedFiscal;
    
    @Column(name = "desc_sped_fiscal")
    private String descSpedFiscal;
}

