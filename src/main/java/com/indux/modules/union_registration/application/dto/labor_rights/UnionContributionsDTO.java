package com.indux.modules.union_registration.application.dto.labor_rights;

import lombok.Data;

@Data
public class UnionContributionsDTO {
    
    private BeneficioEstruturadoDTO ContribuicaoSindicalEmpregado;
    private BeneficioEstruturadoDTO ContribuicaoPatronal; 
    private BeneficioEstruturadoDTO ContribuicaoPatronalEducativa;
}


