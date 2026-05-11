package com.indux.core.domain.service.module;

import com.indux.core.application.dto.module.StepModuleDTO;

public interface ModuleStepService {
    /**
     * Cria uma nova etapa em um módulo.
     * Apenas desenvolvedores tem acesso.
     * @param moduleID identificador do módulo
     * @param dto      dados da nova etapa
     */
    void createModuleStep(String moduleID, StepModuleDTO dto);

    /**
     * Atualiza o SLA de uma etapa específica.
     * @param moduleID   identificador do módulo
     * @param stepNumber número da etapa
     * @param newSLA     novo valor de SLA em horas
     */
    void updateStepSLA(String moduleID, int stepNumber, int newSLA);

    /**
     * Atualiza o SLA de todas as etapas de um módulo.
     * @param moduleID identificador do módulo
     * @param newSLA   novo valor de SLA em horas
     */
    void updateAllStepsSLA(String moduleID, int newSLA);

    /**
     * Atualiza informações de uma etapa específica.
     * @param moduleID    identificador do módulo
     * @param SLA         novo valor de SLA em horas
     * @param stepNumber  número da etapa
     * @param description nova descrição da etapa
     */
    void update(String moduleID, Integer SLA, Integer stepNumber, String description);
}
