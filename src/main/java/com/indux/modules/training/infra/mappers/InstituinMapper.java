package com.indux.modules.training.infra.mappers;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.training.application.dto.InstituinRequestDTO;
import com.indux.modules.training.application.dto.InstituinResponseDTO;
import com.indux.modules.training.application.dto.ProposalRequestDTO;
import com.indux.modules.training.application.dto.ProposalResponseDTO;
import com.indux.modules.training.domain.entity.InstituinEntity;
import com.indux.modules.training.domain.entity.TrainingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mapper(componentModel = "spring")
public interface InstituinMapper {
        InstituinEntity toEntity(InstituinRequestDTO instituinRequestDTO);

        @Mapping(target = "trainingId", source = "trainings", qualifiedByName = "extractTrainingIds")
        @Mapping(target = "trainingName", source = "trainings", qualifiedByName = "extractTrainingNames")
        InstituinResponseDTO toDto(InstituinEntity instituinEntity);

        @Mapping(target = "trainingId", source = "trainings", qualifiedByName = "extractTrainingIds")
        @Mapping(target = "trainingName", source = "trainings", qualifiedByName = "extractTrainingNames")
        List<InstituinResponseDTO> toDtos(List<InstituinEntity> instituinEntities);
        
        @Named("extractTrainingIds")
        default List<String> extractTrainingIds(List<TrainingEntity> trainings) {
                if (trainings == null) {
                        return null;
                }
                return trainings.stream()
                        .map(TrainingEntity::getId)
                        .collect(Collectors.toList());
        }
        
        @Named("extractTrainingNames")
        default List<String> extractTrainingNames(List<TrainingEntity> trainings) {
                if (trainings == null) {
                        return null;
                }
                return trainings.stream()
                        .filter(Objects::nonNull)
                        .map(TrainingEntity::getName)
                        .collect(Collectors.toList());
        }
        
        @Named("mapProposals")
        default List<ProposalResponseDTO> mapProposals(List<ProposalRequestDTO> proposals, InstituinEntity instituinEntity, AttachmentService attachmentService) {
                if (proposals == null || proposals.isEmpty()) {
                        return List.of();
                }
                
                return IntStream.range(0, proposals.size())
                        .mapToObj(i -> {
                                ProposalRequestDTO proposal = proposals.get(i);
                                AttachmentEntity attachment = mapAttachment(proposal, i, instituinEntity, attachmentService);
                                return new ProposalResponseDTO(
                                        proposal.date(),
                                        proposal.name(),
                                        proposal.observation(),
                                        attachment
                                );
                        })
                        .collect(Collectors.toList());
        }
        
        default AttachmentEntity mapAttachment(ProposalRequestDTO proposal, int index, InstituinEntity instituinEntity, AttachmentService attachmentService) {
                if (proposal.file() != null && !proposal.file().isEmpty()) {
                        List<AttachmentEntity> attachments = attachmentService.createAttachmentsFromMultipartFiles(
                                List.of(proposal.file()),
                                "training/instituin/proposals"
                        );
                        return attachments.isEmpty() ? null : attachments.getFirst();
                } else if (instituinEntity != null && 
                          instituinEntity.getProposals() != null && 
                          index < instituinEntity.getProposals().size() &&
                          instituinEntity.getProposals().get(index).attachment() != null) {
                        return instituinEntity.getProposals().get(index).attachment();
                }
                return null;
        }
}