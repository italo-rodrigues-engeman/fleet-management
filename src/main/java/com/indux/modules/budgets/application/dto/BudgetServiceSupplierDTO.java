package com.indux.modules.budgets.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.indux.core.domain.model.modules.AttachmentEntity;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetServiceSupplierDTO {
    private String fornecedor;
    private BigDecimal valor;
    private String url;
    private String observacao;
    private String unidadeMedida;
    private List<MultipartFile> anexos;
    private Boolean selecionado;

}
