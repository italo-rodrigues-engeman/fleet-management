package com.indux.modules.clients.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientRequestDTO {
    
    @NotBlank(message = "O nome é obrigatório")
    private String name;
    
    @NotBlank(message = "O CNPJ é obrigatório")
    private String cnpj;
    
    @NotBlank(message = "O mercado é obrigatório")
    private String mercado;
    
    @NotBlank(message = "O endereço da planta é obrigatório")
    private String enderecoPlanta;
    
    @NotBlank(message = "A cidade é obrigatória")
    private String cidade;
    
    @NotBlank(message = "O estado é obrigatório")
    private String estado;
    
    // Novos campos do cliente
    private String telefones;
    private String site;
    private String email;
    private Long idRamo;
    private String ramo;
    
    // Imagem da logo
    private MultipartFile logo;
    
    // Documento de due diligence
    private String dueDiligenceDoc;
    
    // Fiscais com novos campos
    @Valid
    private List<ClientFiscalDTO> fiscais;
    
    // Filiais
    @Valid
    private List<ClientBranchDTO> filiais;
    
    @NotNull(message = "O status é obrigatório")
    private com.indux.modules.clients.domain.model.ClientStatus status;
    
    private String observacao;
    
    private Boolean dueDiligentes;
    
    private String icj;

    // Plantas industriais diretamente relacionadas ao cliente
    @Valid
    private List<IndustrialPlantCreateDTO> plantas;
} 