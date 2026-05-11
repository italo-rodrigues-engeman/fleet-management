package com.indux.modules.faq.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "tb_setor")
@Getter
@Setter
public class Setor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;
    
    @Column(name = "status", nullable = false)
    private Boolean status = true;
    
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
    
    @OneToMany(mappedBy = "setor", fetch = FetchType.LAZY)
    private List<Tema> temas;
    
    // Construtores
    public Setor() {}
    
    public Setor(String nome) {
        this.nome = nome;
        this.status = true;
    }
    
    public Setor(String nome, Boolean status) {
        this.nome = nome;
        this.status = status;
    }

} 