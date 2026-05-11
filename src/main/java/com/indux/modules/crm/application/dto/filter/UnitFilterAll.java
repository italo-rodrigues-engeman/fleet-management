package com.indux.modules.crm.application.dto.filter;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record UnitFilterAll(
        String client,
        String name,
        String type,
        String city,
        String state,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        Boolean status
) {

}
