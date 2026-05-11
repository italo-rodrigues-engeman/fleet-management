package com.indux.core.application.dto.auth;

import java.util.List;

public record RegisterBatchRequest(
        List<String> matriculas,
        String senha
) {
}
