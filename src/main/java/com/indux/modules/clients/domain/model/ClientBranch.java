package com.indux.modules.clients.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_filiais_cliente")
public class ClientBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Client client;

    @Column(nullable = false)
    private String cnpj;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String endereco;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String telefone;

    @Column(name = "tipo_filial", columnDefinition = "TEXT")
    private String tipoFilial;

    @Column(name = "ramo_filial", columnDefinition = "TEXT")
    private String ramo;

    @ElementCollection
    @CollectionTable(name = "tb_contatos_filial", joinColumns = @JoinColumn(name = "filial_id"))
    private java.util.List<Contact> contatos = new java.util.ArrayList<>();
} 