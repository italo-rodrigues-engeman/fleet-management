package com.indux.core.application.mapper;

import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.modules.StepModule;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class ModuleResponseMapper {
    public ModuleResponseDTO toDTO(Modulo modulo,
                                   UUID userId,
                                   boolean isAdmin,
                                   boolean isFavorite) {
        return new ModuleResponseDTO(
                modulo.getId(),
                modulo.getName(),
                modulo.getDescription(),
                modulo.getStepsQuantity(),
                mapperStep(modulo.getConfigEtapas()),
                mapperPermissions(modulo, userId),
                isAdmin,
                modulo.isGerente(userId),
                isFavorite
        );
    }

    private Set<ModuleResponseDTO.EtapaDTO> mapperStep(Set<StepModule> etapas) {
        Set<ModuleResponseDTO.EtapaDTO> dtos = new HashSet<>(etapas.size());
        for (StepModule etapa : etapas) {
            dtos.add(new ModuleResponseDTO.EtapaDTO(
                    etapa.getEtapa(),
                    etapa.getNome(),
                    etapa.getTempo()));
        }
        return dtos;
    }

    private ModuleResponseDTO.PermissoesUsuarioDTO mapperPermissions(Modulo modulo, UUID userId) {
        var permissao = modulo.getPermissoes().stream()
                .filter(p -> p.getResponsable().equals(userId))
                .findFirst()
                .orElse(null);

        if (permissao == null) {
            return new ModuleResponseDTO.PermissoesUsuarioDTO(
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    Set.of());
        }

        return new ModuleResponseDTO.PermissoesUsuarioDTO(
                new HashSet<>(permissao.getStepsAllowed()),
                new HashSet<>(permissao.getActions()),
                new HashSet<>(permissao.getRegionais()),
                new HashSet<>(permissao.getProjetos())
                );
    }
}