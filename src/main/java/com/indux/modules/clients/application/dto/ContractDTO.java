package com.indux.modules.clients.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {
    private Integer id;
    private Integer rateio;
    private String costCenterName;
    private Integer megaId;
    private String projectName;
    private String contractManager;
    private String client;
    private String codeSap;
    private LocalDate dataAssinatura;
    private LocalDate dataFim;
    private java.util.List<PlatformDTO> plataformas;
} 