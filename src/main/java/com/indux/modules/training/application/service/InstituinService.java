package com.indux.modules.training.application.service;

import com.indux.core.domain.service.AttachmentService;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.training.application.dto.InstituinRequestDTO;
import com.indux.modules.training.application.dto.InstituinResponseDTO;
import com.indux.modules.training.application.dto.ProposalResponseDTO;
import com.indux.modules.training.domain.entity.InstituinEntity;
import com.indux.modules.training.domain.entity.TrainingEntity;
import com.indux.modules.training.domain.repository.InstituinRepository;
import com.indux.modules.training.domain.repository.TrainingRepository;
import com.indux.modules.training.infra.mappers.InstituinMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstituinService {

    private final InstituinRepository instituinRepository;
    private final TrainingRepository trainingRepository;
    private final InstituinMapper instituinMapper;
    private final AttachmentService attachmentService;

    public InstituinService(InstituinRepository instituinRepository, TrainingRepository trainingRepository, InstituinMapper instituinMapper, AttachmentService attachmentService) {
        this.instituinRepository = instituinRepository;
        this.trainingRepository = trainingRepository;
        this.instituinMapper = instituinMapper;
        this.attachmentService = attachmentService;
    }

    public void createInstituin(InstituinRequestDTO instituinRequestDTO) {
        List<TrainingEntity> trainingEntities = trainingRepository.findAllById(instituinRequestDTO.trainingId());
        InstituinEntity entity = instituinMapper.toEntity(instituinRequestDTO);
        entity.setTrainings(trainingEntities);
        List<ProposalResponseDTO> processedProposals = processProposals(instituinRequestDTO, null);
        entity.setProposals(processedProposals);
        instituinRepository.save(entity);
    }

    private List<ProposalResponseDTO> processProposals(InstituinRequestDTO instituinRequestDTO, InstituinEntity instituinEntity) {
        return instituinMapper.mapProposals(instituinRequestDTO.proposals(), instituinEntity, attachmentService);
    }

    public InstituinResponseDTO findInstituinById(String id) {
        InstituinEntity entity = instituinRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Instituinção não encontrado"));
        return instituinMapper.toDto(entity);
    }

    public Page<InstituinResponseDTO> findAllInstituin(String search,List<String> trainingIds,Pageable pageable) {
        Page<InstituinEntity> entity;
        
        entity = instituinRepository.findBySearchAndTrainingIds(search, trainingIds, pageable);

        
        var dto = instituinMapper.toDtos(entity.getContent());
        return new PageImpl<>(dto, pageable, entity.getTotalElements());
    }

    public void updateInstituin(String id, InstituinRequestDTO instituinRequestDTO) {
        InstituinEntity entity = instituinRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Instituinção não encontrado"));
        var updatedEntity = instituinMapper.toEntity(instituinRequestDTO);
        List<TrainingEntity> trainingEntities = trainingRepository.findAllById(instituinRequestDTO.trainingId());
        updatedEntity.setTrainings(trainingEntities);
        updatedEntity.setId(id);
        List<ProposalResponseDTO> processedProposals = processProposals(instituinRequestDTO, entity);
        updatedEntity.setProposals(processedProposals);
        instituinRepository.save(updatedEntity);
    }
}
