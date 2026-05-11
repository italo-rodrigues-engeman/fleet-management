package com.indux.modules.clients.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndustrialPlantDTO {
    private Long id;
    private String tipoPlanta;
    private String endereco;
    private String linkMaps;
    private List<ContactDTO> contatos;
    private List<String> linksExternos;
    private List<String> fotos;
    private String observacoes;
} 