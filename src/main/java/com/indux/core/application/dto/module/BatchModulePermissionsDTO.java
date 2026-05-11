package com.indux.core.application.dto.module;

import java.util.Set;
import java.util.UUID;

public record BatchModulePermissionsDTO(
        UUID userId,
        Set<ModulePermissionInput> permissoes
) {
}

