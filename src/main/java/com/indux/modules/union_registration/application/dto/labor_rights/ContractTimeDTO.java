package com.indux.modules.union_registration.application.dto.labor_rights;

import lombok.Data;

@Data
public class ContractTimeDTO {
    
    // 8 - Tempo de contrato
    private String tempo30x30; // 30x30
    private String tempo45x45; // 45x45
    private String tempo30NaoRenovaveis; // 30 não renováveis
    private String outro; // outros tempos de contrato
}
