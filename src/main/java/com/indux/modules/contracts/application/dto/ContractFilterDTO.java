package com.indux.modules.contracts.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractFilterDTO {
    private String cliente;
    private String nomeProjeto;
    private String regional;
    private Boolean ativo;
    private String os;
    private String codSap;
    private String gestorInterno;
    private String gestorCliente;
} 