package com.indux.modules.clients.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tb_clientes")
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome")
    private String name;
    
    @Column(name = "cnpj", unique = true)
    private String cnpj;
    
    @Column(name = "mercado")
    private String mercado;
    
    @Column(name = "endereco_cliente", columnDefinition = "TEXT")
    private String enderecoPlanta;
    
    @Column(name = "cidade")
    private String cidade;
    
    @Column(name = "estado")
    private String estado;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientFiscal> fiscais = new ArrayList<>();
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientBranch> filiais = new ArrayList<>();
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ClientStatus status;
    
    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;
    
    @Column(name = "due_diligentes", columnDefinition = "boolean default false")
    private Boolean dueDiligentes;
    
    @Column(name = "data_due_diligentes")
    private LocalDateTime dataDueDiligentes;
    
    @Column(name = "due_diligence_doc", columnDefinition = "TEXT")
    private String dueDiligenceDoc;
    
    @Column(name = "icj")
    private String icj;
    
    @Column(name = "logo_marca", columnDefinition = "TEXT")
    private String logoMarca;
    
    @Column(name = "telefones", columnDefinition = "TEXT")
    private String telefones;
    
    @Column(name = "site", columnDefinition = "TEXT")
    private String site;
    
    @Column(name = "email", columnDefinition = "TEXT")
    private String email;
    
    @Column(name = "ramo", columnDefinition = "TEXT")
    private String ramo;
    
    @Column(name = "id_ramo")
    private Long idRamo;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<IndustrialPlant> plantas = new java.util.ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (dueDiligentes == null) {
            dueDiligentes = false;
        }
        if (status == null) {
            status = ClientStatus.INATIVO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
} 