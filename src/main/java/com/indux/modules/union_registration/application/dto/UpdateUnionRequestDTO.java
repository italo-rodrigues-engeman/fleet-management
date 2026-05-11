package com.indux.modules.union_registration.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUnionRequestDTO {
    
    @Size(max = 1000, message = "O nome completo deve ter no máximo 1000 caracteres")
    private String nomeCompletoSindicato;
    
    @Size(max = 18, message = "O CNPJ deve ter no máximo 18 caracteres")
    private String cnpj;
    
    @Size(max = 20, message = "O código CNES deve ter no máximo 20 caracteres")
    private String codigoCnes;
    
    private String tipo;
    
    @Size(max = 1000, message = "A categoria representada deve ter no máximo 1000 caracteres")
    private String categoriaRepresentada;
    
    private String abrangenciaTerritorial;
    
    private List<String> ufSede;
    
    private List<String> municipioSede;
    
    private List<String> ufsAtendidas;
    
    private List<String> municipiosAtendidos;
    
    private String observacoesTerritoriais;
    
    @Size(max = 1000, message = "O logradouro deve ter no máximo 1000 caracteres")
    private String logradouro;
    
    @Size(max = 20, message = "O número deve ter no máximo 20 caracteres")
    private String numero;
    
    @Size(max = 2, message = "A UF deve ter no máximo 2 caracteres")
    private String uf;
    
    @Size(max = 10, message = "O CEP deve ter no máximo 10 caracteres")
    private String cep;
    
    @Size(max = 1000, message = "A cidade deve ter no máximo 1000 caracteres")
    private String cidade;
    
    @Size(max = 20, message = "O telefone principal deve ter no máximo 20 caracteres")
    private String telefonePrincipal;
    
    @Size(max = 20, message = "O telefone 2 deve ter no máximo 20 caracteres")
    private String telefone2;
    
    @Email(message = "Email institucional deve ter um formato válido")
    private String emailInstitucional;
    
    @Email(message = "Email 2 deve ter um formato válido")
    private String email2;
    
    @Size(max = 1000, message = "O site deve ter no máximo 1000 caracteres")
    private String site;
    
    @Size(max = 1000, message = "A rede social deve ter no máximo 1000 caracteres")
    private String redeSocial;
    
    private String situacaoMte;
    
    private LocalDate dataUltimaAtualizacaoMte;
    
    @Size(max = 1000, message = "O nome do presidente deve ter no máximo 1000 caracteres")
    private String presidenteAtual;
    
    private LocalDate mandatoInicio;
    
    private LocalDate mandatoFim;
    
    private List<MultipartFile> documentosAnexos;
    
    private String observacoes;
    
    private String statusRegistro;
}

