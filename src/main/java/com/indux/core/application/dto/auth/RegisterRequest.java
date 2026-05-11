package com.indux.core.application.dto.auth;

import com.indux.core.application.dto.module.ModulePermissionInput;
import com.mongodb.lang.Nullable;

import java.util.Set;

public record RegisterRequest(String cpf, String email, boolean isPJ,
                              Set<ModulePermissionInput> modules, String nome, String telefone, @Nullable Long role) {
}
