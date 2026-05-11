package com.indux.modules.clients.application.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientBranchDTO {
    private String cnpj;
    private String nome;
    private String endereco;
    private String telefone;

    // Novos campos
    private String tipoFilial;  // ex: matriz, filial operacional, etc.
    private String ramo;        // ramo/segmento específico da filial

    @Valid
    private List<ContactDTO> contatos;
} 