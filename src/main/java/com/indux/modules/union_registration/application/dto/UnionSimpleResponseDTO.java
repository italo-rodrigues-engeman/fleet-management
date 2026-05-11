package com.indux.modules.union_registration.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnionSimpleResponseDTO {
    
    private String id;
    private String nomeCompletoSindicato;
    private String tipo;
    private String statusRegistro;
    private String uf;
    private String cidade;
}
