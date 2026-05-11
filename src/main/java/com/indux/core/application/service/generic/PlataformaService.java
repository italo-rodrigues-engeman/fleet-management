package com.indux.core.application.service.generic;

import com.indux.core.application.dto.generic.PlataformaDTO;
import com.indux.core.domain.model.generic.Plataforma;
import com.indux.core.domain.repository.generic.PlataformaRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlataformaService {
    private final PlataformaRepository repository;

    public PlataformaService(PlataformaRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca todas as plataformas únicas ordenadas por nome
     */
    @Cacheable(cacheNames = "platforms", key = "'all'")
    public List<Plataforma> getAllPlatforms() {
        return repository.findAllDistinctOrderByNomePlataforma();
    }

    /**
     * Busca apenas os nomes únicos das plataformas
     */
    @Cacheable(cacheNames = "platforms", key = "'names'")
    public List<PlataformaDTO> getAllPlatformNames() {
        return repository.findDistinctNomePlataforma()
                .stream()
                .map(PlataformaDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca apenas os nomes únicos das plataformas que contenham o texto especificado
     */
    public List<PlataformaDTO> getPlatformNamesContaining(String nome) {
        return repository.findDistinctNomePlataformaContaining(nome)
                .stream()
                .map(PlataformaDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca plataforma por ID
     */
    public Plataforma getPlatformById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plataforma não encontrada com ID: " + id));
    }

    /**
     * Busca plataforma por nome
     */
    public Plataforma getPlatformByName(String nomePlataforma) {
        return repository.findByNomePlataforma(nomePlataforma);
    }

    /**
     * Busca plataformas que contenham o nome especificado
     */
    public List<Plataforma> searchPlatformsByName(String nomePlataforma) {
        return repository.findByNomePlataformaContainingIgnoreCase(nomePlataforma);
    }
} 