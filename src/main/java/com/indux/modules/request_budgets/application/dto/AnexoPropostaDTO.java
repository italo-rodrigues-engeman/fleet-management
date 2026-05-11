package com.indux.modules.request_budgets.application.dto;

import com.indux.core.domain.model.modules.AttachmentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnexoPropostaDTO {
    private String anexoTipo;
    private List<AttachmentEntity> anexoProposta;
}

