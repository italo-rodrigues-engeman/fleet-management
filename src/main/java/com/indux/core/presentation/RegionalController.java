package com.indux.core.presentation;

import com.indux.core.application.dto.generic.RegionalDTO;
import com.indux.core.domain.model.employee.Regional;
import com.indux.core.domain.service.generic.RegionalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/regional")
@RequiredArgsConstructor
@Slf4j
public class RegionalController {

    private final RegionalService regionalService;

    /**
     * Busca todas as regionais com suas filiais
     */
    @GetMapping("/all")
    public ResponseEntity<List<RegionalDTO>> getAllRegionaisWithFiliais() {

        List<Regional> regionais = regionalService.findAllWithFiliais();

        
        List<RegionalDTO> regionaisDTO = regionais.stream()
                .map(RegionalDTO::fromEntity)
                .collect(Collectors.toList());
        

        return ResponseEntity.ok(regionaisDTO);
    }

    /**
     * Busca uma regional específica por ID com suas filiais
     */
    @GetMapping("/{id}")
    public ResponseEntity<RegionalDTO> getRegionalById(@PathVariable Long id) {
        Optional<Regional> regional = regionalService.findByIdWithFiliais(id);
        if (regional.isPresent()) {
            Regional reg = regional.get();
            return ResponseEntity.ok(RegionalDTO.fromEntity(reg));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Busca regionais por nome (contendo)
     */
    @GetMapping("/search")
    public ResponseEntity<List<RegionalDTO>> searchRegionais(@RequestParam String nome) {
        List<Regional> regionais = regionalService.findByRegionalContainingIgnoreCase(nome);
        List<RegionalDTO> regionaisDTO = regionais.stream()
                .map(RegionalDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(regionaisDTO);
    }

    /**
     * Busca uma regional específica por nome
     */
    @GetMapping("/by-name/{nome}")
    public ResponseEntity<RegionalDTO> getRegionalByName(@PathVariable String nome) {
        Optional<Regional> regional = regionalService.findByRegional(nome);
        return regional.map(RegionalDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cria uma nova regional
     */
    @PostMapping
    public ResponseEntity<RegionalDTO> createRegional(@RequestBody Regional regional) {
        Regional savedRegional = regionalService.save(regional);
        return ResponseEntity.ok(RegionalDTO.fromEntity(savedRegional));
    }

    /**
     * Atualiza uma regional existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<RegionalDTO> updateRegional(@PathVariable Long id, @RequestBody Regional regional) {
        Optional<Regional> existingRegional = regionalService.findByIdWithFiliais(id);
        if (existingRegional.isPresent()) {
            regional.setId(id);
            Regional updatedRegional = regionalService.save(regional);
            return ResponseEntity.ok(RegionalDTO.fromEntity(updatedRegional));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Deleta uma regional
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegional(@PathVariable Long id) {
        Optional<Regional> regional = regionalService.findByIdWithFiliais(id);
        if (regional.isPresent()) {
            regionalService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Busca todas as filiais de uma regional por ID
     */
    @GetMapping("/{id}/filiais")
    public ResponseEntity<RegionalDTO> getFiliaisByRegionalId(@PathVariable Long id) {
        List<Regional> regionais = regionalService.findFiliaisByRegionalId(id);
        if (!regionais.isEmpty()) {
            return ResponseEntity.ok(RegionalDTO.fromEntity(regionais.get(0)));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Busca todas as filiais de uma regional por nome
     */
    @GetMapping("/by-name/{nome}/filiais")
    public ResponseEntity<RegionalDTO> getFiliaisByRegionalName(@PathVariable String nome) {
        List<Regional> regionais = regionalService.findFiliaisByRegionalName(nome);
        if (!regionais.isEmpty()) {
            return ResponseEntity.ok(RegionalDTO.fromEntity(regionais.get(0)));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Exemplo de uso: Busca apenas as filiais de uma regional específica
     * Este endpoint demonstra como usar a relação entre Regional e Filial
     */
    @GetMapping("/{id}/filiais-only")
    public ResponseEntity<List<RegionalDTO.FilialDTO>> getFiliaisOnlyByRegionalId(@PathVariable Long id) {
        List<RegionalDTO.FilialDTO> filiais = regionalService.getFiliaisByRegionalId(id);
        return ResponseEntity.ok(filiais);
    }

    /**
     * Endpoint de debug para verificar dados das regionais e filiais
     */
    @GetMapping("/debug/info")
    public ResponseEntity<String> getDebugInfo() {
        StringBuilder info = new StringBuilder();
        
        List<Regional> allRegionais = regionalService.findAll();
        List<Regional> regionaisWithFiliais = regionalService.findAllWithFiliais();
        
        info.append("=== DEBUG INFO ===\n");
        info.append("Total de regionais: ").append(allRegionais.size()).append("\n");
        info.append("Regionais com filiais carregadas: ").append(regionaisWithFiliais.size()).append("\n");
        
        for (Regional regional : regionaisWithFiliais) {
            info.append("Regional ID: ").append(regional.getId())
                .append(", Nome: ").append(regional.getRegional())
                .append(", Filiais: ").append(regional.getFiliais().size()).append("\n");
            
            for (var filial : regional.getFiliais()) {
                info.append("  - Filial ID: ").append(filial.getBranchId())
                    .append(", Nome: ").append(filial.getBranchName())
                    .append(", Regional ID: ").append(filial.getRegional() != null ? filial.getRegional().getId() : "NULL")
                    .append("\n");
            }
        }
        
        return ResponseEntity.ok(info.toString());
    }

    /**
     * Endpoint para testar a relação regional-filial com dados específicos
     */
    @GetMapping("/test/relation/{regionalId}")
    public ResponseEntity<String> testRegionalRelation(@PathVariable Long regionalId) {
        StringBuilder info = new StringBuilder();
        
        Optional<Regional> regional = regionalService.findByIdWithFiliais(regionalId);
        
        info.append("=== TESTE DE RELAÇÃO ===\n");
        info.append("Regional ID: ").append(regionalId).append("\n");
        
        if (regional.isPresent()) {
            Regional reg = regional.get();
            info.append("Regional encontrada: ").append(reg.getRegional()).append("\n");
            info.append("Total de filiais: ").append(reg.getFiliais().size()).append("\n");
            
            if (reg.getFiliais().isEmpty()) {
                info.append("AVISO: Regional não tem filiais associadas!\n");
                info.append("Verifique se a coluna Id_regional em tb_filiais está preenchida.\n");
            } else {
                info.append("Filiais encontradas:\n");
                for (var filial : reg.getFiliais()) {
                    info.append("  - ID: ").append(filial.getBranchId())
                        .append(", Nome: ").append(filial.getBranchName())
                        .append(", Regional ID: ").append(filial.getRegional() != null ? filial.getRegional().getId() : "NULL")
                        .append("\n");
                }
            }
        } else {
            info.append("Regional não encontrada!\n");
        }
        
        return ResponseEntity.ok(info.toString());
    }
}
