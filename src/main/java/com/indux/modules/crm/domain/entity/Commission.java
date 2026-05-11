package com.indux.modules.crm.domain.entity;


import com.indux.core.domain.model.modules.AttachmentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commission {

    private String id;

    private BigDecimal initialValue;

    private BigDecimal finalValue;

    private Double percentage;
}
