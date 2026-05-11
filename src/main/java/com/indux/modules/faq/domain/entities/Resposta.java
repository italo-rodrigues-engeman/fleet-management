package com.indux.modules.faq.domain.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_resposta")
public class Resposta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome", nullable = false, length = 1000)
    private String nome;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pergunta_id", nullable = false)
    private Pergunta pergunta;
    
    @Column(name = "contrato", nullable = false)
    private Integer contrato;
    
    @Column(name = "status", nullable = false)
    private Boolean status = true;
    
    // Construtores
    public Resposta() {}
    
    public Resposta(String nome, Pergunta pergunta, Integer contrato) {
        this.nome = nome;
        this.pergunta = pergunta;
        this.contrato = contrato;
        this.status = true;
    }
    
    public Resposta(String nome, Pergunta pergunta, Integer contrato, Boolean status) {
        this.nome = nome;
        this.pergunta = pergunta;
        this.contrato = contrato;
        this.status = status;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public Pergunta getPergunta() {
        return pergunta;
    }
    
    public void setPergunta(Pergunta pergunta) {
        this.pergunta = pergunta;
    }
    
    public Integer getContrato() {
        return contrato;
    }
    
    public void setContrato(Integer contrato) {
        this.contrato = contrato;
    }
    
    public Boolean getStatus() {
        return status;
    }
    
    public void setStatus(Boolean status) {
        this.status = status;
    }
} 