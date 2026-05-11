package com.indux.modules.union_registration.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaborContractSummaryDTO {
    private String id;
    private String numeroRegistro;
    private LocalDate dataInicioVigencia;
    private LocalDate dataFimVigencia;
    private String status;
    private String tipoInstrumento;
}
