package com.indux.modules.ppu.application.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor @NoArgsConstructor @Getter @Setter
public class DivergenceRecord {
    String idItem;
    LocalDate data;
    String justificativa;
    Integer totalPrevisto;
    Integer totalRealizado;
}
