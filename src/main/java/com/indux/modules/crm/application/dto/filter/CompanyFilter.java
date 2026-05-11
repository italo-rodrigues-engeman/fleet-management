package com.indux.modules.crm.application.dto.filter;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
public record CompanyFilter(
        String cnpj,
        String name,
        String sector,
        String market,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate registrationDate,
        Boolean status
) {
}
