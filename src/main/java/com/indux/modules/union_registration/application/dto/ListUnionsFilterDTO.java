package com.indux.modules.union_registration.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListUnionsFilterDTO {
    
    private String nomeCompletoSindicato;
    private String cnpj;
    private String tipo;
    private String categoriaRepresentada;
    private String ufSede;
    private String municipioSede;
    private String situacaoMte;
    private String presidenteAtual;
    private String statusRegistro;
    private String cidade;
    private String uf;
    private String ufAtendida;
}

