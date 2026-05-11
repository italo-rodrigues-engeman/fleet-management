package com.indux.core.application.dto.generic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeFiltersDTO {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilialDTO {
        private Long codigo_filial;
        private String nome_filial;
    }

    private List<FilialDTO> filiais;
    private List<String> situacoes;
    private List<String> estados;
    private List<String> graus_instrucao;
    private List<Integer> qtd_dependentes;
} 