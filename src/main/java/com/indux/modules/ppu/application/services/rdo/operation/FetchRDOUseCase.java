package com.indux.modules.ppu.application.services.rdo.operation;

import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.dtos.rdo.RDOResponse;
import com.indux.modules.ppu.application.services.rdo.helper.RDOTotalPlannedHelper;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FetchRDOUseCase extends RDOTotalPlannedHelper {
    private final RDORepository repository;
    private final PPURepository ppuRepository;
    private final GetEmployeeUseCase employeeUseCase;

    public FetchRDOUseCase(RDORepository repository, PPURepository ppuRepository, GetEmployeeUseCase employeeUseCase) {
        this.repository = repository;
        this.ppuRepository = ppuRepository;
        this.employeeUseCase = employeeUseCase;
    }

    public RDOResponse execute(String id) {
        RDOEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RDO não encontrada no sistema."));
        return toResponse(entity);
    }

    public RDOResponse toResponse(RDOEntity entity) {
        PPUEntity ppuEntity = ppuRepository.findById(entity.getPpuId())
                .orElseThrow(() -> new RuntimeException("PPU não encontrada para este RDO."));
        return toResponse(entity, ppuEntity);
    }

    public RDOResponse toResponse(RDOEntity entity, PPUEntity ppuEntity) {
        enrichCreatorPosition(entity);
        return RDOResponse.fromEntity(
                entity,
                getUnusedServices(ppuEntity, entity),
                ppuEntity.getShiftSchedule(),
                ppuEntity.getMandatorySAMC(),
                isDuplicationAllowedByPPU(ppuEntity, entity)
        );
    }

    public RDOResponse toSimpleResponse(RDOEntity entity) {
        return RDOResponse.fromEntity(entity, List.of(), List.of(), false, false);
    }

    private void enrichCreatorPosition(RDOEntity entity) {
        if (entity.getCreatorRegistration() == null) return;
        var creator = employeeUseCase.getEmployeeByMatricula(entity.getCreatorRegistration());
        entity.setCreatorPosition(creator.getCargo());
    }

    @NotNull
    private static List<String> getUnusedServices(PPUEntity ppu, RDOEntity entity) {
        return ppu.getServices().stream()
                .filter(svc -> entity.getServices().stream()
                        .noneMatch(entSvc -> entSvc.getServiceID().equals(svc.getId())))
                .map(svc -> svc.getGenericNumber() + " - " + svc.getName())
                .collect(Collectors.toList());
    }

    private boolean isDuplicationAllowedByPPU(PPUEntity ppuEntity, RDOEntity rdoEntity) {
        if (ppuEntity == null || rdoEntity == null) return false;
        if (!Boolean.TRUE.equals(ppuEntity.getAllowsRDODuplication())) return false;
        if (ppuEntity.getStatus() != DocumentStatus.ABERTO) return false;
        if (ppuEntity.getPlatforms() == null || ppuEntity.getPlatforms().isEmpty()) return false;
        return ppuEntity.getPlatforms().contains(rdoEntity.getPlatform());
    }
}
