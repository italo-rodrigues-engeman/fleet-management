package com.indux.core.application.dto.module;

import java.util.List;

public record MultipleUsersModulePermissionsDTO(
        List<UserModulePermission> permissoes
) {
    public record UserModulePermission(
            String responsable,
            List<Integer> filiais,
            List<Integer> contratos,
            List<Integer> stepsAllowed,
            List<Integer> actions,
            boolean group
    ) {}
}
