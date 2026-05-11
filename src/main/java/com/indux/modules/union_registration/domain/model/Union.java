package com.indux.modules.union_registration.domain.model;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.StepLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "unions")
public class Union {
    
    @Id
    private String id;
    
    private Integer codeID;
    
    private String nomeCompletoSindicato;
    private String cnpj;
    private String codigoCnes;
    private String tipo;
    private String categoriaRepresentada;
    private String abrangenciaTerritorial;
    private List<String> ufSede;
    private List<String> municipioSede;
    private List<String> ufsAtendidas;
    private List<String> municipiosAtendidos;
    private String observacoesTerritoriais;
    private String logradouro;
    private String numero;
    private String uf;
    private String cep;
    private String cidade;
    private String telefonePrincipal;
    private String telefone2;
    private String emailInstitucional;
    private String email2;
    private String site;
    private String redeSocial;
    private String situacaoMte;
    private LocalDate dataUltimaAtualizacaoMte;
    private String presidenteAtual;
    private LocalDate mandatoInicio;
    private LocalDate mandatoFim;
    private List<AttachmentEntity> documentosAnexos;
    private String observacoes;
    private LocalDateTime dataCriacao;
    private String usuarioCriacao;
    private String statusRegistro;
    
    // Auditoria
    private List<StepLog> stepLog;
    private LocalDateTime dataUltimaAtualizacao;
    private String usuarioUltimaAtualizacao;
    
    public Union(Integer codeID, String nomeCompletoSindicato, String cnpj, String codigoCnes,
                 String tipo, String categoriaRepresentada, String abrangenciaTerritorial,
                 List<String> ufSede, List<String> municipioSede, List<String> ufsAtendidas, 
                 List<String> municipiosAtendidos, String observacoesTerritoriais,
                 String logradouro, String numero, String uf, String cep, String cidade,
                 String telefonePrincipal, String telefone2, String emailInstitucional, String email2,
                 String site, String redeSocial, String situacaoMte, LocalDate dataUltimaAtualizacaoMte, 
                 String presidenteAtual, LocalDate mandatoInicio, LocalDate mandatoFim, 
                 List<AttachmentEntity> documentosAnexos, String observacoes, String usuarioCriacao, 
                 String statusRegistro) {
        this.codeID = codeID;
        this.nomeCompletoSindicato = nomeCompletoSindicato;
        this.cnpj = cnpj;
        this.codigoCnes = codigoCnes;
        this.tipo = tipo;
        this.categoriaRepresentada = categoriaRepresentada;
        this.abrangenciaTerritorial = abrangenciaTerritorial;
        this.ufSede = ufSede;
        this.municipioSede = municipioSede;
        this.ufsAtendidas = ufsAtendidas;
        this.municipiosAtendidos = municipiosAtendidos;
        this.observacoesTerritoriais = observacoesTerritoriais;
        this.logradouro = logradouro;
        this.numero = numero;
        this.uf = uf;
        this.cep = cep;
        this.cidade = cidade;
        this.telefonePrincipal = telefonePrincipal;
        this.telefone2 = telefone2;
        this.emailInstitucional = emailInstitucional;
        this.email2 = email2;
        this.site = site;
        this.redeSocial = redeSocial;
        this.situacaoMte = situacaoMte;
        this.dataUltimaAtualizacaoMte = dataUltimaAtualizacaoMte;
        this.presidenteAtual = presidenteAtual;
        this.mandatoInicio = mandatoInicio;
        this.mandatoFim = mandatoFim;
        this.documentosAnexos = documentosAnexos;
        this.observacoes = observacoes;
        this.dataCriacao = LocalDateTime.now();
        this.usuarioCriacao = usuarioCriacao;
        this.statusRegistro = statusRegistro != null ? statusRegistro : "ATIVO";
    }
}
