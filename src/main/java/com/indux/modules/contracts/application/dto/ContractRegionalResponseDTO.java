package com.indux.modules.contracts.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractRegionalResponseDTO {
    
    private Long mega_id;
    private String cliente;
    private FilialDTO filial;
    private String centro_custo_nome;
    private String clientName;
    private Long rateio_id;
    private String gestor_interno_contrato;
    private Long id;
    private String nome_projeto;
    private List<String> coordenadores_contrato;
    private Boolean ativo;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilialDTO {
        private String razao_social;
        private Long codigo_filial;
        private String nome_filial;
    }
}
