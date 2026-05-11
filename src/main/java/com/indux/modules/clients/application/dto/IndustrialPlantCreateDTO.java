package com.indux.modules.clients.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndustrialPlantCreateDTO {
    private String tipoPlanta;          // plataforma offshore, refinaria, etc.
    private String endereco;
    private String linkMaps;
    private List<ContactDTO> contatos;
    private List<String> linksExternos;
    private List<MultipartFile> fotos;  // array de imagens
    private String observacoes;
} 