package com.indux.modules.ppu.infra.mapper;

import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.dtos.requests.PPURequest;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.infra.mapper.ppu.AccessoryKitMapper;
import com.indux.modules.ppu.infra.mapper.ppu.EquipmentLineMapper;
import com.indux.modules.ppu.infra.mapper.ppu.ServiceLineMapper;
import com.indux.modules.ppu.infra.mapper.ppu.SteelCableMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Map;

@Mapper(componentModel = "spring",
        uses = { EquipmentLineMapper.class, ServiceLineMapper.class, SteelCableMapper.class, AccessoryKitMapper.class  },
        imports = { DocumentStatus.class })
public interface PPUEntityMapper {

    @Mapping(source = "record.regionalID", target = "regionalId")
    @Mapping(source = "record.regionalName", target = "regionalNome")
    @Mapping(source = "record.plataformas", target = "platforms")
    @Mapping(target = "platformFrequencies", expression = "java(record.frequenciasPlataformas() != null ? record.frequenciasPlataformas().entrySet().stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, e -> com.indux.modules.ppu.application.dtos.RDOFrequencyDTO.toDomain(e.getValue()))) : null)")
    @Mapping(source = "record.descricao", target = "generalObservation")
    @Mapping(source = "record.horarios", target = "shiftSchedule")
    @Mapping(source = "record.caboDeTurma", target = "teamLeader")
    @Mapping(source = "record.qtdSinaleiros", target = "signalmenQuantity")
    @Mapping(source = "record.tipo", target = "type")
    @Mapping(source = "record.responsavel", target = "responsible")
    @Mapping(source = "record.dataPPU", target = "dateRange")
    @Mapping(source = "record.samcObrigatorio", target = "mandatorySAMC")
    @Mapping(source = "record.auditableConfig", target = "auditableConfig")
    @Mapping(source = "record.periodoMedicao", target = "measurementPeriod")
    @Mapping(source = "record.equipamentos", target = "equipments")
    @Mapping(source = "record.cabosAcos", target = "steelCables" )
    @Mapping(source = "record.servicos", target = "services")
    @Mapping(source = "record.kitAcessorios", target = "accessoryKits")
    @Mapping(target = "codeID", expression = "java(generatedPpuCodeId)")
    @Mapping(target = "clientId", expression = "java((Long) contract.get(\"cliente\"))")
    @Mapping(target = "status", expression = "java(DocumentStatus.ABERTO)")
    @Mapping(target = "craneControls", expression = "java(record.controleGuindaste().stream().map(com.indux.modules.ppu.domain.entities.item.CraneControl::fromDTO).toList())")
    @Mapping(target = "steelCableControls", expression = "java(record.controleCaboAco().stream().map(com.indux.modules.ppu.domain.entities.item.SteelCableControl::fromDTO).toList())")
    PPUEntity toEntity(PPURequest record, @Context Map<String, Object> contract, @Context Long generatedPpuCodeId);

    @Mapping(source = "regionalID", target = "regionalId")
    @Mapping(source = "regionalName", target = " regionalNome")
    @Mapping(source = "plataformas", target = "platforms")
    @Mapping(target = "platformFrequencies", expression = "java(record.frequenciasPlataformas() != null ? record.frequenciasPlataformas().entrySet().stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, e -> com.indux.modules.ppu.application.dtos.RDOFrequencyDTO.toDomain(e.getValue()))) : null)")
    @Mapping(source = "descricao", target = "generalObservation")
    @Mapping(source = "horarios", target = "shiftSchedule")
    @Mapping(source = "samcObrigatorio", target = "mandatorySAMC")
    @Mapping(source = "tipo", target = "type")
    @Mapping(source = "responsavel", target = "responsible")
    @Mapping(source = "dataPPU", target = "dateRange")
    @Mapping(source = "auditableConfig", target = "auditableConfig")
    @Mapping(source = "periodoMedicao", target = "measurementPeriod")
    @Mapping(source = "equipamentos", target = "equipments")
    @Mapping(source = "cabosAcos", target = "steelCables" )
    @Mapping(source = "servicos", target = "services")
    @Mapping(source = "clienteID", target = "clientId")
    @Mapping(source = "kitAcessorios", target = "accessoryKits")
    @Mapping(target = "status", expression = "java(DocumentStatus.ABERTO)")
    @Mapping(source = "apelido", target = "nickname")
    @Mapping(source = "vinteQuatroHoras", target = "is24hType")
    @Mapping(source = "valorSinaleiros", target = "signalmenValue")
    @Mapping(source = "projetoId", target = "projectId")
    @Mapping(source = "temSupervisorBordo", target = "hasSupervisorOnBoard")
    @Mapping(source = "permiteDuplicarRDO", target = "allowsRDODuplication")
    PPUEntity baseForm(PPURequest record);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "baseForm")
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "equipments", ignore = true)
    @Mapping(target = "steelCables", ignore = true)
    @Mapping(target = "accessoryKits", ignore = true)
    @Mapping(target = "lines", ignore = true)
    @Mapping(target = "availableType", ignore = true)
    @Mapping(target = "shiftSchedule", ignore = true)
    @Mapping(target = "craneControls", ignore = true)
    @Mapping(target = "steelCableControls", ignore = true)
    void updateFromRequest(PPURequest record, @MappingTarget PPUEntity entity);
}
