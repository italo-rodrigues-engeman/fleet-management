package com.indux.modules.union_registration.application.mapper;

import com.indux.modules.union_registration.application.dto.CreateLaborContractAddendumRequestDTO;
import com.indux.modules.union_registration.application.dto.LaborContractAddendumResponseDTO;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {LaborRightsMapper.class}, imports = {LocalDateTime.class})
public interface LaborContractAddendumMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "contratoTrabalhistaId", target = "contratoTrabalhistaId")
    @Mapping(target = "sequencia", ignore = true) // Calculada automaticamente no UseCase
    @Mapping(source = "attachments", target = "arquivosAnexos")
    @Mapping(target = "dataCriacao", expression = "java(LocalDateTime.now())")
    @Mapping(source = "usuarioCriacao", target = "usuarioCriacao")
    @Mapping(target = "statusRegistro", constant = "ATIVO")
    @Mapping(target = "dataUltimaAtualizacao", ignore = true)
    @Mapping(target = "usuarioUltimaAtualizacao", ignore = true)
    LaborContractAddendum toEntity(CreateLaborContractAddendumRequestDTO request, 
                                  String contratoTrabalhistaId, 
                                  String usuarioCriacao,
                                  List<com.indux.core.domain.model.modules.AttachmentEntity> attachments);
    
    LaborContractAddendumResponseDTO toResponseDTO(LaborContractAddendum addendum);
}
