package com.indux.modules.union_registration.application.dto.labor_rights;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialSecurityDTO {
    
    private String previdenciaComplementar;
    private String encargosSociaisAdicionais;
    private String programaSeguroEmprego;
}
