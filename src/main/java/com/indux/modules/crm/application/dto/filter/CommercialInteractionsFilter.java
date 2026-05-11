package com.indux.modules.crm.application.dto.filter;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record CommercialInteractionsFilter(
        String company,
        Boolean status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
) {
}
