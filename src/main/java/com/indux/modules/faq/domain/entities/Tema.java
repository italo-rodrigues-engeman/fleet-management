package com.indux.modules.faq.domain.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "tb_tema")
public class Tema {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pergunta> perguntas;
    
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "status", nullable = false)
    private Boolean status = true;
    
    @Column(name = "ordenacao", nullable = false)
    private Integer ordenacao;
    
    // Construtores
    public Tema() {}
    
    public Tema(String nome, Setor setor) {
        this.nome = nome;
        this.setor = setor;
    }
    
    public Tema(String nome, Setor setor, String descricao, Boolean status, Integer ordenacao) {
        this.nome = nome;
        this.setor = setor;
        this.descricao = descricao;
        this.status = status;
        this.ordenacao = ordenacao;
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
    
    public Setor getSetor() {
        return setor;
    }
    
    public void setSetor(Setor setor) {
        this.setor = setor;
    }
    
    public List<Pergunta> getPerguntas() {
        return perguntas;
    }
    
    public void setPerguntas(List<Pergunta> perguntas) {
        this.perguntas = perguntas;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
    
    public Integer getOrdenacao() {
        return ordenacao;
    }
    
    public void setOrdenacao(Integer ordenacao) {
        this.ordenacao = ordenacao;
    }
} 