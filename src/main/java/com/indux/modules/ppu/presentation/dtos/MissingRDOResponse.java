package com.indux.modules.ppu.presentation.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @AllArgsConstructor @NoArgsConstructor
public class MissingRDOResponse {
    private String rdo;
    private LocalDate data;
    private String regional;
    private String plataforma;
    private String nomeContrato;
    private Long contratoId;
}
