package com.indux.modules.union_registration.application.dto.labor_rights;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaborRightsDTO {
    
    // 4 - Salários
    private SalaryDTO salary = new SalaryDTO();
    
    // 5 - Benefícios
    private BenefitsDTO benefits = new BenefitsDTO();
    
    // 5.1 - Benefícios de Saúde
    private HealthBenefitsDTO healthBenefits = new HealthBenefitsDTO();
    
    // 6 - Jornada de Trabalho
    private WorkScheduleDTO workSchedule = new WorkScheduleDTO();
    
    // 7 - Tipos de Escala
    private WorkShiftTypesDTO workShiftTypes = new WorkShiftTypesDTO();
    
    // 8 - Tempo de Contrato
    private ContractTimeDTO contractTime = new ContractTimeDTO();
    
    // 8 - Folgas e Licenças
    private TimeOffBenefitsDTO timeOffBenefits = new TimeOffBenefitsDTO();
    
    // 9 - Saúde e Segurança
    private HealthSafetyDTO healthSafety = new HealthSafetyDTO();
    
    // 10 - Adicionais
    private AdditionalBenefitsDTO additionalBenefits = new AdditionalBenefitsDTO();
    
    // 11 - Transporte
    private TransportationDTO transportation = new TransportationDTO();
    
    // 12 - Anuênio
    private SeniorityBonusDTO seniorityBonus = new SeniorityBonusDTO();
    
    // 12 - Condições de Trabalho
    private WorkplaceConditionsDTO workplaceConditions = new WorkplaceConditionsDTO();
    
    // 13 - Procedimentos Disciplinares
    private DisciplinaryProceduresDTO disciplinaryProcedures = new DisciplinaryProceduresDTO();
    
    // 14 - Contribuições Sindicais
    private UnionContributionsDTO unionContributions = new UnionContributionsDTO();
}
