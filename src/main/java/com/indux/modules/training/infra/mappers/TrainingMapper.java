package com.indux.modules.training.infra.mappers;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.training.application.dto.FiliaisHCMRequest;
import com.indux.modules.training.application.dto.FiliaisHCMResponse;
import com.indux.modules.training.application.dto.TrainingRequest;
import com.indux.modules.training.application.dto.TrainingResponse;
import com.indux.modules.training.domain.entity.TrainingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface TrainingMapper {
    TrainingEntity toEntity(TrainingRequest dto);
    TrainingResponse toDto(TrainingEntity entity);
    @Mapping(target = "filiais", ignore = true)
    List<TrainingResponse> toDtos(List<TrainingEntity> entityList);
    
    @Mapping(target = "filialHCM", source = "updated.filialHCM")
    @Mapping(target = "type", source = "updated.type")
    @Mapping(target = "mandatory", source = "updated.mandatory")
    @Mapping(target = "target", source = "updated.target")
    @Mapping(target = "time", source = "updated.time")
    @Mapping(target = "link", source = "updated.link")
    @Mapping(target = "attachment", source = "attachment")
    FiliaisHCMResponse createResponse(FiliaisHCMResponse updated, AttachmentEntity attachment);
    
    @Mapping(target = "filialHCM", source = "original.filialHCM")
    @Mapping(target = "type", source = "original.type")
    @Mapping(target = "mandatory", source = "original.mandatory")
    @Mapping(target = "target", source = "original.target")
    @Mapping(target = "time", source = "original.time")
    @Mapping(target = "link", source = "original.link")
    @Mapping(target = "attachment", source = "original.attachment")
    @Mapping(target = "idFilial", expression = "java(orgData != null ? (List<Integer>) orgData.get(\"idFilial\") : (original.filialHCM() != null ? original.filialHCM().stream().map(Integer::valueOf).toList() : null))")
    @Mapping(target = "nomeFilial", expression = "java(orgData != null ? (List<String>) orgData.get(\"nomeFilial\") : null)")
    @Mapping(target = "idProjeto", expression = "java(orgData != null ? (List<Long>) orgData.get(\"idProjeto\") : null)")
    @Mapping(target = "nomeProjeto", expression = "java(orgData != null ? (List<String>) orgData.get(\"nomeProjeto\") : null)")
    @Mapping(target = "idContrato", expression = "java(orgData != null ? (List<Long>) orgData.get(\"idContrato\") : null)")
    @Mapping(target = "nomeContrato", expression = "java(orgData != null ? (List<String>) orgData.get(\"nomeContrato\") : null)")
    @Mapping(target = "idRegional", expression = "java(orgData != null ? (List<Long>) orgData.get(\"idRegional\") : null)")
    @Mapping(target = "nomeRegional", expression = "java(orgData != null ? (List<String>) orgData.get(\"nomeRegional\") : null)")
    FiliaisHCMResponse createResponseWithOrgData(FiliaisHCMResponse original, Map<String, Object> orgData);
    
    @Mapping(target = "filialHCM", source = "original.filialHCM")
    @Mapping(target = "type", source = "original.type")
    @Mapping(target = "mandatory", source = "original.mandatory")
    @Mapping(target = "client", source = "original.client")
    @Mapping(target = "clientName", source = "clientName")
    @Mapping(target = "norm", source = "original.norm")
    @Mapping(target = "target", source = "original.target")
    @Mapping(target = "time", source = "original.time")
    @Mapping(target = "link", source = "original.link")
    @Mapping(target = "attachment", source = "original.attachment")
    @Mapping(target = "idFilial", source = "original.idFilial")
    @Mapping(target = "nomeFilial", source = "original.nomeFilial")
    @Mapping(target = "idProjeto", source = "original.idProjeto")
    @Mapping(target = "nomeProjeto", source = "original.nomeProjeto")
    @Mapping(target = "idContrato", source = "original.idContrato")
    @Mapping(target = "nomeContrato", source = "original.nomeContrato")
    @Mapping(target = "idRegional", source = "original.idRegional")
    @Mapping(target = "nomeRegional", source = "original.nomeRegional")
    @Mapping(target = "workload", source = "original.workload")
    @Mapping(target = "duration", source = "original.duration")
    @Mapping(target = "validity", source = "original.validity")
    @Mapping(target = "requeriment", source = "original.requeriment")
    @Mapping(target = "minimun", source = "original.minimun")
    @Mapping(target = "provisional", source = "original.provisional")
    @Mapping(target = "provisionalValidity", source = "original.provisionalValidity")
    @Mapping(target = "workloadRecycling", source = "original.workloadRecycling")
    @Mapping(target = "durationRecycling", source = "original.durationRecycling")
    @Mapping(target = "validityRecycling", source = "original.validityRecycling")
    FiliaisHCMResponse updateClientName(FiliaisHCMResponse original, String clientName);
}
