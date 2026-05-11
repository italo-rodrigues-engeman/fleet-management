package com.indux.modules.clients.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientFiscalDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cargo;
    private String observacoes;
} 