package com.indux.modules.crm.application.dto.filter;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record LeadFilter(
        String state,
        String city,
        String name,
        String function,
        String decisionMakeLevel,
        String unit,
        String company,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        Boolean status
) {
}
