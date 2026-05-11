package com.indux.modules.cdi.aplication.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.cdi.aplication.dtos.ActionCDIDTO;
import com.indux.modules.cdi.aplication.dtos.CreateActionDTO;
import com.indux.modules.cdi.aplication.dtos.DevCDIDTO;
import com.indux.modules.cdi.aplication.dtos.DevelopmentDTO;
import com.indux.modules.cdi.domain.entities.mongo.ActionCDIEntity;
import com.indux.modules.cdi.domain.repositories.mongo.ActionCDIRepository;
import com.indux.modules.cdi.infra.mappers.ActionCDIMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActionCDIService {
    private final ActionCDIRepository actionCDIRepository;
    private final ActionCDIMapper actionCDIMapper;

    public ActionCDIService(ActionCDIRepository actionCDIRepository, ActionCDIMapper actionCDIMapper) {
        this.actionCDIRepository = actionCDIRepository;
        this.actionCDIMapper = actionCDIMapper;
    }

    public void createActionCdi(CreateActionDTO dto) {
        var action = actionCDIMapper.toEntity(dto);
        actionCDIRepository.save(action);
    }

    public ActionCDIDTO findByCdiId(String cdiId){
        var action = actionCDIRepository.findByCdiId(cdiId).orElseThrow(() -> new ModuleNotFoundFailure("Ideia não emcontrada"));
        return actionCDIMapper.toDTO(action);
    }

    public void updateActionCdi(String id,List<DevelopmentDTO> details) {
        var action = actionCDIRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Ideia não emcontrada"));
        action.setDevelopment(details);
        actionCDIRepository.save(action);
    }

    public void updateDev(String id, DevCDIDTO devDto) {
        var existing = actionCDIRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Ideia não encontrada"));

        mergeNonNullFields(devDto, existing);

        actionCDIRepository.save(existing);
    }

    public void mergeNonNullFields(DevCDIDTO dto, ActionCDIEntity entity) {
        if (dto.request() != null) entity.setRequest(dto.request());
        if (dto.approval() != null) entity.setApproval(dto.approval());
        if (dto.development() != null) entity.setDevelopment(dto.development());
        if (dto.test() != null) entity.setTest(dto.test());
        if (dto.finalTest() != null) entity.setFinalTest(dto.finalTest());
        if (dto.avaliationDescription() != null) entity.setAvaliationDescription(dto.avaliationDescription());
        if (dto.reason() != null) entity.setReason(dto.reason());
        if (dto.fixes() != null) entity.setFixes(dto.fixes());
        if (dto.improves() != null) entity.setImproves(dto.improves());
        if(dto.status() != null) entity.setStatus(dto.status());
    }

    public List<ActionCDIDTO> getAllDev(){
        var action = actionCDIRepository.findAllByIsDevTrue();
        return actionCDIMapper.toDTOList(action);
    }
}
