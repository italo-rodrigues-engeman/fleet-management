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
public class BeneficioEstruturadoDTO {
    private Boolean aplicavel;
    private String periodicidade; // Mensal/Anual/Diário/Quando necessário
    private BigDecimal valorPercentual; // Valor % (Salário base / Salário Mínimo)
    private String valorTipo; // Salário base ou Salário Mínimo
    private BigDecimal valorReais; // Valor R$
    private String observacao;
}

