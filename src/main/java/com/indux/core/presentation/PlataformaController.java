package com.indux.core.presentation;

import com.indux.core.application.dto.generic.PlataformaDTO;
import com.indux.core.application.service.generic.PlataformaService;
import com.indux.core.domain.model.generic.Plataforma;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platforms")
@CrossOrigin(origins = "*")
public class PlataformaController {
    
    private final PlataformaService plataformaService;

    public PlataformaController(PlataformaService plataformaService) {
        this.plataformaService = plataformaService;
    }

    /**
     * Busca apenas os nomes únicos das plataformas (sem ID)
     * Parâmetro opcional 'nome' para filtrar resultados
     */
    @GetMapping
    public ResponseEntity<List<PlataformaDTO>> getAllPlatformNames(@RequestParam(required = false) String nome) {
        List<PlataformaDTO> platforms;
        if (nome != null && !nome.trim().isEmpty()) {
            platforms = plataformaService.getPlatformNamesContaining(nome.trim());
        } else {
            platforms = plataformaService.getAllPlatformNames();
        }
        return ResponseEntity.ok(platforms);
    }

    /**
     * Busca apenas os nomes únicos das plataformas que contenham o texto especificado
     */
    @GetMapping("/search-names")
    public ResponseEntity<List<PlataformaDTO>> searchPlatformNames(@RequestParam String nome) {
        List<PlataformaDTO> platforms = plataformaService.getPlatformNamesContaining(nome);
        return ResponseEntity.ok(platforms);
    }

    /**
     * Busca todas as plataformas completas (com ID)
     */
    @GetMapping("/complete")
    public ResponseEntity<List<Plataforma>> getAllPlatformsComplete() {
        List<Plataforma> platforms = plataformaService.getAllPlatforms();
        return ResponseEntity.ok(platforms);
    }

    /**
     * Busca plataforma por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Plataforma> getPlatformById(@PathVariable Long id) {
        Plataforma platform = plataformaService.getPlatformById(id);
        return ResponseEntity.ok(platform);
    }

    /**
     * Busca plataformas que contenham o nome especificado
     */
    @GetMapping("/search")
    public ResponseEntity<List<Plataforma>> searchPlatformsByName(@RequestParam String nome) {
        List<Plataforma> platforms = plataformaService.searchPlatformsByName(nome);
        return ResponseEntity.ok(platforms);
    }


} 