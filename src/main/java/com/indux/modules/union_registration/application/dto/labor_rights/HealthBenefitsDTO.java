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
public class HealthBenefitsDTO {
    
    // PLANO DE SAÚDE
    private PlanoSaudeOdontologicoDTO planoSaude = new PlanoSaudeOdontologicoDTO();
    
    // PLANO ODONTOLÓGICO
    private PlanoSaudeOdontologicoDTO planoOdontologico = new PlanoSaudeOdontologicoDTO();
    
    // SEGURO DE VIDA
    private SeguroVidaDTO seguroVida = new SeguroVidaDTO();
}

