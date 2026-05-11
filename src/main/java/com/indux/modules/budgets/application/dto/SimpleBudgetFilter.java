package com.indux.modules.budgets.application.dto;

import lombok.Data;

@Data
public class SimpleBudgetFilter {
    private String acOs;
    private String setor;
    private Integer oportunidade;
    private String nomeOportunidade;
    private String orcamentista;
    private Long clienteId;
    private java.util.List<String> status;
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private java.time.LocalDate startDate;
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private java.time.LocalDate endDate;
}
