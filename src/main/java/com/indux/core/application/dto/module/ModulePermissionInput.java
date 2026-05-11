package com.indux.core.application.dto.module;

import com.mongodb.lang.Nullable;

import java.util.Set;
import java.util.UUID;

public record ModulePermissionInput(
        UUID moduleId,
        UUID user,
        boolean isGroup,
        Set<Integer> etapasResponsaveis,
        Set<Integer> acoes,
        Set<Integer> regionais,
        Set<Integer> projetos
) {
}
