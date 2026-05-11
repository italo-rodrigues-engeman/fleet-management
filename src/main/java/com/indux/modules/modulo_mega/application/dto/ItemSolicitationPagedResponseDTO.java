package com.indux.modules.modulo_mega.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemSolicitationPagedResponseDTO {
    private Page<ItemSolicitationListDTO> page;
    private long countTechnicalValidation;
    private long countTaxValidation;
    private long countRegistration;
    private long countRejected;
    private long countRegistered;
}
