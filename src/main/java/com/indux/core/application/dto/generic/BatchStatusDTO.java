package com.indux.core.application.dto.generic;

import com.mongodb.lang.Nullable;

import java.util.List;

public record BatchStatusDTO(@Nullable String observacao, List<String> itens) {
}
