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
public class TransportationDTO {
    
    // 11 - Transporte
    
    // Vale Transporte (lei 7418 de 85)
    private BeneficioEstruturadoDTO valeTransporte;
    
    // Auxílio Transporte
    private BeneficioEstruturadoDTO auxilioTransporte;
    
    // Fretado
    private BeneficioEstruturadoDTO fretado;
}
