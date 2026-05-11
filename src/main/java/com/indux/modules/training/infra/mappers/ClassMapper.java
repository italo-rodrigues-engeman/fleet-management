package com.indux.modules.training.infra.mappers;

import com.indux.modules.training.application.dto.ClassRequest;
import com.indux.modules.training.application.dto.ClassResponse;
import com.indux.modules.training.domain.entity.ClassEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ClassMapper {
    ClassEntity toEntity(ClassRequest classRequest);
    ClassRequest toDTO(ClassEntity classEntity);
    
    @Mapping(target = "nomeFilial", ignore = true)
    @Mapping(target = "idProjeto", ignore = true)
    @Mapping(target = "nomeProjeto", ignore = true)
    @Mapping(target = "idContrato", ignore = true)
    @Mapping(target = "nomeContrato", ignore = true)
    @Mapping(target = "idRegional", ignore = true)
    @Mapping(target = "nomeRegional", ignore = true)
    ClassResponse toResponseDTO(ClassEntity classEntity);
    
    List<ClassRequest> toDTOs(List<ClassEntity> classEntityList);
    List<ClassResponse> toResponseDTOs(List<ClassEntity> classEntityList);
    
    @Mapping(target = "id", source = "original.id")
    @Mapping(target = "filialHCM", source = "original.filialHCM")
    @Mapping(target = "training", source = "original.training")
    @Mapping(target = "dateStrart", source = "original.dateStrart")
    @Mapping(target = "dateEnd", source = "original.dateEnd")
    @Mapping(target = "finished", source = "original.finished")
    @Mapping(target = "observation", source = "original.observation")
    @Mapping(target = "collaborators", source = "original.collaborators")
    @Mapping(target = "status", source = "original.status")
    @Mapping(target = "attachment", source = "original.attachment")
    @Mapping(target = "nomeFilial", expression = "java(orgData != null ? (String) orgData.get(\"nomeFilial\") : null)")
    @Mapping(target = "idProjeto", expression = "java(orgData != null ? (Long) orgData.get(\"idProjeto\") : null)")
    @Mapping(target = "nomeProjeto", expression = "java(orgData != null ? (String) orgData.get(\"nomeProjeto\") : null)")
    @Mapping(target = "idContrato", expression = "java(orgData != null ? (Long) orgData.get(\"idContrato\") : null)")
    @Mapping(target = "nomeContrato", expression = "java(orgData != null ? (String) orgData.get(\"nomeContrato\") : null)")
    @Mapping(target = "idRegional", expression = "java(orgData != null ? (Long) orgData.get(\"idRegional\") : null)")
    @Mapping(target = "nomeRegional", expression = "java(orgData != null ? (String) orgData.get(\"nomeRegional\") : null)")
    ClassResponse enrichWithOrganizationData(ClassResponse original, Map<String, Object> orgData);
}