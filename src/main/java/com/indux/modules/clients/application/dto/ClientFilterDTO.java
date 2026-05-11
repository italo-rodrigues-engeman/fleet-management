package com.indux.modules.clients.application.dto;

import lombok.Data;

@Data
public class ClientFilterDTO {
    private String enderecoPlanta;
    private String fiscal;
    private String cnpj;
    private String cidade;
    private String estado;
    private String tipoCliente; // Público / Privado
    private com.indux.modules.clients.domain.model.ClientStatus status;
} 