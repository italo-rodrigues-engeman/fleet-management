package com.indux.modules.union_registration.application.dto.labor_rights;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalBenefitsDTO {
    
    // 10 - Adicionais
    // NOTURNO
    private BeneficioEstruturadoDTO adicionalNoturno;
    
    // INSALUBRIDADE
    private BeneficioEstruturadoDTO adicionalInsalubridade;
    
    // PERICULOSIDADE
    private BeneficioEstruturadoDTO adicionalPericulosidade;
    
    // SOBREAVISO (20%)
    private BeneficioEstruturadoDTO adicionalSobreaviso;
    
    // PRONTIDÃO (33,33%)
    private BeneficioEstruturadoDTO adicionalProntidao;

    //HRA
    private BeneficioEstruturadoDTO adicionalHra;
}
