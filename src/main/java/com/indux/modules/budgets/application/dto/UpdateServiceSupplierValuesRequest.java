package com.indux.modules.budgets.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateServiceSupplierValuesRequest {
    private List<ServiceSupplierValueDTO> valores;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServiceSupplierValueDTO {
        private String nomeServico;
        private String fornecedor;
        private Boolean selecionado;
        private BigDecimal valorConsiderado;
        private Double quantidade;
        private BigDecimal valorOrcamento;
    }
}
