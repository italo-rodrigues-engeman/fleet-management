package com.indux.modules.clients.application.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UpdateClientRequestDTO {
    private String name;

    private String cnpj;

    private String mercado;

    private String enderecoPlanta;

    private String cidade;

    private String estado;

    private List<ClientFiscalDTO> fiscais;

    private com.indux.modules.clients.domain.model.ClientStatus status;

    private String observacao;

    private Boolean dueDiligentes;

    private String icj;

    private String logoMarca;

    private MultipartFile logo;

    // Documento de due diligence
    private String dueDiligenceDoc;

    private String email;

    private String ramo;
} 