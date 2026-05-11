package com.indux.modules.union_registration.application.dto.labor_rights;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkScheduleDTO {
    
    // 6 - Hora Extra
    // H. EXTRA 1 - SEG. A SEX
    private BeneficioEstruturadoDTO horaExtra1;
    
    // H. EXTRA 2 - SÁBADO
    private BeneficioEstruturadoDTO horaExtra2;
    
    // H. EXTRA 3 - DOMINGO / FERIADO
    private BeneficioEstruturadoDTO horaExtra3;
    
    private String bancoHoras;
    private String controlePonto;
    
    // Tipo de contratação permitido
    private List<String> tiposContratacaoPermitidos; // Trabalho Intermitente, Estagiário, Jovem Aprendiz, Contrato por prazo indeterminado
}