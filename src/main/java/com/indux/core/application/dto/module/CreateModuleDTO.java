package com.indux.core.application.dto.module;

import java.util.Set;

public record CreateModuleDTO(String nome,
                              int quantidadeEtapas,
                              Set<StepModuleDTO> etapas,
                              Set<ModulePermissionInput> permissoes,
                              Set<Integer> setores
) {
}
