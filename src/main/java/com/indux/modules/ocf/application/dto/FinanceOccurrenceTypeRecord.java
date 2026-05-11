package com.indux.modules.ocf.application.dto;

import jakarta.annotation.Nullable;

public record FinanceOccurrenceTypeRecord(String nome, @Nullable Long ref) {
}
