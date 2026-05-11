package com.indux.modules.clients.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    private Long id;
    private String name;
    private String cnpj;
    private String mercado;
    private String ramo;
    private String enderecoPlanta;
    private String cidade;
    private String estado;
    private List<ClientFiscalDTO> fiscais;
    private com.indux.modules.clients.domain.model.ClientStatus status;
    private String observacao;
    private Boolean dueDiligentes;
    private String icj;
    private String logoMarca;
    private String dueDiligenceDoc;

    private String telefones;
    private String site;
    private String email;
    private Long idRamo;
    private List<ClientBranchDTO> filiais;
    private List<IndustrialPlantDTO> plantas;
    private List<ContractDTO> contratos;
} 