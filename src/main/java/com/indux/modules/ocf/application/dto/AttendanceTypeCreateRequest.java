package com.indux.modules.ocf.application.dto;

import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public record AttendanceTypeCreateRequest(
        @Nullable String nome,
        @Nullable List<String> nomes,
        @Nullable Long ref
) {
    public List<String> allNames() {
        List<String> result = new ArrayList<>();
        if (nomes != null) {
            result.addAll(nomes);
        }
        if (nome != null && !nome.isBlank()) {
            result.add(nome);
        }
        return result;
    }
}

