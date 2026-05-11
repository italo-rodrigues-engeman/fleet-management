package com.indux.modules.union_registration.application.mapper;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.union_registration.application.dto.CreateLaborContractRequestDTO;
import com.indux.modules.union_registration.application.dto.LaborContractResponseDTO;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.model.Union;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {LaborRightsMapper.class}, imports = {LocalDateTime.class})
public interface LaborContractMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "attachments", target = "arquivoInstrumento")
    @Mapping(source = "usuarioCriacao", target = "usuarioCriacao")
    @Mapping(target = "dataCriacao", expression = "java(LocalDateTime.now())")
    @Mapping(target = "statusRegistro", constant = "ATIVO")
    LaborContract toEntity(CreateLaborContractRequestDTO request, List<AttachmentEntity> attachments, String usuarioCriacao);
    
    @Mapping(source = "arquivoInstrumento", target = "arquivosInstrumento")
    LaborContractResponseDTO toResponseDTO(LaborContract laborContract);
    
    @Mapping(source = "laborContract.id", target = "id")
    @Mapping(source = "laborContract.arquivoInstrumento", target = "arquivosInstrumento")
    @Mapping(source = "laborContract.abrangenciaTerritorial", target = "abrangenciaTerritorial")
    @Mapping(source = "laborContract.observacoesTerritoriais", target = "observacoesTerritoriais")
    @Mapping(source = "laborContract.situacaoMTE", target = "situacaoMTE")
    @Mapping(source = "laborContract.dataCriacao", target = "dataCriacao")
    @Mapping(source = "laborContract.usuarioCriacao", target = "usuarioCriacao")
    @Mapping(source = "laborContract.statusRegistro", target = "statusRegistro")
    @Mapping(source = "laborContract.stepLog", target = "stepLog")
    @Mapping(source = "laborContract.dataUltimaAtualizacao", target = "dataUltimaAtualizacao")
    @Mapping(source = "laborContract.usuarioUltimaAtualizacao", target = "usuarioUltimaAtualizacao")
    @Mapping(source = "union.nomeCompletoSindicato", target = "nomeSindicato")
    @Mapping(target = "siglaSindicato", ignore = true)
    LaborContractResponseDTO toResponseDTOWithUnionInfo(LaborContract laborContract, Union union);
    
    @Mapping(source = "laborContract.arquivoInstrumento", target = "arquivosInstrumento")
    @Mapping(source = "addendums", target = "addendums")
    LaborContractResponseDTO toResponseDTOWithAddendums(LaborContract laborContract, List<LaborContractAddendum> addendums);
}
