package com.indux.modules.request_budgets.application.dto;

import com.indux.core.domain.model.modules.AttachmentEntity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBudgetOSRequest {
    @NotNull(message = "Número da OS é obrigatório")
    private String numeroOs;

    private String status;
    private List<AttachmentEntity> anexoOs;
    private String cnpjDueDiligence;
    private String inscEstadual;
    private String cpfduediligence;
}

