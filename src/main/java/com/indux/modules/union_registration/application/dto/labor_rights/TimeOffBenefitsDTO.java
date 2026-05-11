package com.indux.modules.union_registration.application.dto.labor_rights;

import lombok.Data;

@Data
public class TimeOffBenefitsDTO {
    
    // 8 - Folgas e Licenças
    private String ferias;
    private String licencas;
    private String avisoPrevio;
    private String estabilidade;
    private String aleitamentoMaterno;
}