package com.indux.modules.union_registration.application.dto.labor_rights;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeguroVidaDTO {
    private BeneficioEstruturadoDTO beneficio;
    
    // Campos específicos do Seguro de Vida
    private BigDecimal valorPremio; // Valor do Prêmio R$
}


