package com.indux.core.application.service.module;

import com.indux.core.application.dto.module.StepModuleDTO;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.modules.StepModule;
import com.indux.core.domain.repository.module.ModuleRepository;
import com.indux.core.domain.service.module.ModuleStepService;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class ModuleStepServiceImpl implements ModuleStepService {
    private final ModuleRepository repository;

    public ModuleStepServiceImpl(ModuleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void createModuleStep(String moduleID, StepModuleDTO dto) {
        Modulo modulo = repository.findById(UUID.fromString(moduleID))
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado: " + moduleID));
        StepModule step = StepModule.builder()
                .etapa(dto.numEtapa())
                .tempo(dto.tempoAceitavel())
                .nome(dto.nome())
                .build();
        modulo.getConfigEtapas().add(step);
        modulo.setStepsQuantity(modulo.getConfigEtapas().size());
    }

    @Override
    public void updateStepSLA(String moduleID, int numEtapa, int newSLA) {
        Modulo modulo = repository.findById(UUID.fromString(moduleID))
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado: " + moduleID));
        StepModule step = modulo.getConfigEtapas()
                .stream()
                .filter(s -> s.getEtapa() == numEtapa)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Etapa " + numEtapa + " não encontrada no módulo " + moduleID));
        step.setTempo(newSLA);
        step.setModuloId(modulo.getId());
    }

    @Override
    public void updateAllStepsSLA(String moduleID, int newSLA) {
        Modulo modulo = repository.findById(UUID.fromString(moduleID))
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado: " + moduleID));
        modulo.getConfigEtapas().forEach(s -> s.setTempo(newSLA));
        modulo.getConfigEtapas().forEach(s -> s.setModuloId(s.getModuloId()));
    }

    @Override
    public void update(String moduleID, Integer SLA, Integer stepNumber, String description) {
        Modulo modulo = repository.findById(UUID.fromString(moduleID))
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado: " + moduleID));
        StepModule step = modulo.getConfigEtapas()
                .stream()
                .filter(s -> s.getEtapa() == stepNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Etapa " + stepNumber + " não encontrada no módulo " + moduleID));
        step.setTempo((SLA == null || SLA == 0) ? step.getTempo() : SLA);
        step.setDescricao(description.isBlank() ? step.getDescricao() : description);
        step.setModuloId(modulo.getId());
    }
}