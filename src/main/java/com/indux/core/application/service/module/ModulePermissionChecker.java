package com.indux.core.application.service.module;

import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.application.dto.module.ModuleResponseDTO.PermissoesUsuarioDTO;
import com.indux.core.application.mapper.ModuleResponseMapper;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.infra.exception.module.ForbiddenModuleAccessException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class ModulePermissionChecker {

    private final ModuleManagementService moduleManagementService;
    private final ModuleResponseMapper moduleResponseMapper;

    public ModulePermissionChecker(ModuleManagementService moduleManagementService,
            ModuleResponseMapper moduleResponseMapper) {
        this.moduleManagementService = moduleManagementService;
        this.moduleResponseMapper = moduleResponseMapper;
    }

    public PermissoesUsuarioDTO getPermissions(UUID moduleId, UUID userId) {
        var modulo = moduleManagementService.getModuleByID(moduleId);
        ModuleResponseDTO dto = moduleResponseMapper.toDTO(modulo, userId, false, false);
        return dto.permissoesUsuario();
    }

    public boolean hasAccess(UUID moduleId, UUID userId) {
        PermissoesUsuarioDTO perms = getPermissions(moduleId, userId);
        return !perms.regionaisPermitidas().isEmpty() || !perms.etapasPermitidas().isEmpty();
    }

    public void requireAccess(UUID moduleId, UUID userId) {
        if (!hasAccess(moduleId, userId)) {
            throw new ForbiddenModuleAccessException(
                    "Usuário sem permissão de acesso ao módulo: " + moduleId);
        }
    }

    public Set<Integer> getAllowedSteps(UUID moduleId, UUID userId) {
        return getPermissions(moduleId, userId).etapasPermitidas();
    }

    public Set<Integer> getAllowedRegionais(UUID moduleId, UUID userId) {
        return getPermissions(moduleId, userId).regionaisPermitidas();
    }

    public Set<Integer> getAllowedProjects(UUID moduleId, UUID userId) {
        return getPermissions(moduleId, userId).projetosPermitidos();
    }

    public RegionaisAndProjects getAllowedRegionaisAndProjects(UUID moduleId, UUID userId) {
        PermissoesUsuarioDTO perms = getPermissions(moduleId, userId);
        return new RegionaisAndProjects(perms.regionaisPermitidas(), perms.projetosPermitidos());
    }

    public record RegionaisAndProjects(Set<Integer> regionais, Set<Integer> projetos) {
    }
}
