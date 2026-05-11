package com.indux.modules.faq.domain.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "tb_pergunta")
public class Pergunta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome", nullable = false, length = 500)
    private String nome;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id", nullable = false)
    private Tema tema;
    
    @OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Resposta> respostas;
    
    // Construtores
    public Pergunta() {}
    
    public Pergunta(String nome, Tema tema) {
        this.nome = nome;
        this.tema = tema;
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
    
    public Tema getTema() {
        return tema;
    }
    
    public void setTema(Tema tema) {
        this.tema = tema;
    }
    
    public List<Resposta> getRespostas() {
        return respostas;
    }
    
    public void setRespostas(List<Resposta> respostas) {
        this.respostas = respostas;
    }
} 