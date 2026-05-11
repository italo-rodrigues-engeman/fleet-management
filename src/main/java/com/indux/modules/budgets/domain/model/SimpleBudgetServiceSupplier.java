package com.indux.modules.budgets.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.indux.core.domain.model.modules.AttachmentEntity;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleBudgetServiceSupplier {
    private String fornecedor;
    private BigDecimal valor;
    private String url;
    private String observacao;
    private String unidadeMedida;
    private List<AttachmentEntity> anexos;
    private Boolean selecionado;
    private BigDecimal valorConsiderado;
    private Double quantidade;
    private BigDecimal valorOrcamento;
}
