package com.indux.modules.clients.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_planta_industrial")
public class IndustrialPlant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Client client;

    @Column(name = "tipo_planta", columnDefinition = "TEXT")
    private String tipoPlanta;

    @Column(name = "endereco", columnDefinition = "TEXT")
    private String endereco;

    @Column(name = "link_maps", columnDefinition = "TEXT")
    private String linkMaps;

    @ElementCollection
    @CollectionTable(name = "tb_contatos_planta", joinColumns = @JoinColumn(name = "planta_id"))
    private java.util.List<Contact> contatos = new java.util.ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tb_links_externos_planta", joinColumns = @JoinColumn(name = "planta_id"))
    @Column(name = "link")
    private List<String> linksExternos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tb_fotos_planta", joinColumns = @JoinColumn(name = "planta_id"))
    @Column(name = "url_foto")
    private List<String> fotos = new ArrayList<>();

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;
} 