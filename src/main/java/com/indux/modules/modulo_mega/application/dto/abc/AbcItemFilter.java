package com.indux.modules.modulo_mega.application.dto.abc;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AbcItemFilter {
    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private List<Integer> regionalIds;
    private List<Integer> projectIds;
    private List<Integer> contractIds;
    private List<String> types;
}
