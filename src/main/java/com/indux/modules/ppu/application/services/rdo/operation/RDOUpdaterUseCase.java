package com.indux.modules.ppu.application.services.rdo.operation;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.infra.utils.MergeUpdate;
import com.indux.core.infra.utils.ParcialUpdate;
import com.indux.modules.ppu.application.dtos.rdo.RDORecord;
import com.indux.modules.ppu.application.dtos.requests.RDOLineRequest;
import com.indux.modules.ppu.domain.entities.item.CraneControl;
import com.indux.modules.ppu.domain.entities.item.SteelCableControl;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOEmployeeDeparture;
import com.indux.modules.ppu.domain.entities.rdo.RDOEquipment;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLogger;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerType;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.infra.mapper.RDOLoggerUserMapper;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class RDOUpdaterUseCase {
    private final RDOLoggerUserMapper userMapper;
    private RDOUpdaterUseCase(RDOLoggerUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Deprecated
    public RDOEntity updateParcial(@Validated(RDORecord.RDOUpdate.class) RDORecord dto, RDOEntity entity) {
        ParcialUpdate.updateFieldIfNotNull(dto.data(), entity::setDate);
        ParcialUpdate.updateFieldIfNotNull(dto.servicosRealizadosContratada(), entity::setContractorServices);
        ParcialUpdate.updateFieldIfNotNull(dto.registrosObservacao(), entity::setContractorObservations);
        ParcialUpdate.updateFieldIfNotNull(dto.justificativaHoraExtra(), entity::setOvertimeJustification);

        ParcialUpdate.updateListIfNotNull(
                dto.controleGuindaste(),
                CraneControl::fromDTO,
                entity::setCraneControl
        );
        ParcialUpdate.updateListIfNotNull(
                dto.controleCaboAco(),
                SteelCableControl::fromDTO,
                entity::setCableControl
        );
        ParcialUpdate.updateListIfNotNull(
                dto.cabosDeAcos(),
                Function.identity(),
                entity::setSteelCable
        );
        ParcialUpdate.updateListIfNotNull(
                dto.kitsAcessorios(),
                Function.identity(),
                entity::setAccessoryKits
        );
        ParcialUpdate.updateListIfNotNull(
                dto.trabalhoDesembarque(),
                RDOEmployeeDeparture::fromDTO,
                entity::setEmployeeDepartures
        );

        if (dto.servicos() != null) {
            entity.getServices().forEach(oldEnt ->
                    oldEnt.setMergeKey(oldEnt.getServiceID() + ":" + oldEnt.getRegistration())
            );

            List<RDOServiceEntity> newServices = dto.servicos().stream()
                    .flatMap(svcDto -> {
                        var presList = svcDto.presencaColaboradores();
                        if (presList == null || presList.isEmpty()) return Stream.empty();
                        return presList.stream()
                                .map(emp -> {
                                    RDOServiceEntity ent = RDOServiceEntity.fromDTO(svcDto, emp);
                                    ent.setMergeKey(svcDto.id() + ":" + emp.matricula());
                                    return ent;
                                });
                    })
                    .toList();

            List<RDOServiceEntity> mergedServices = MergeUpdate.merge(
                    entity.getServices(),
                    newServices,
                    RDOServiceEntity::getMergeKey,
                    RDOServiceEntity::getMergeKey,
                    RDOServiceEntity::updateFrom,
                    Function.identity(),
                    false
            );
            entity.setServices(mergedServices);
        }
        return entity;
    }

    /**
     * Esse método é utilizado para atualizar totalmente um @RDOEntity.
     * Ele substitui os dados do entity com os dados do record.
     * Substituição completa, não parcial.
     * @param record RDORecord com os dados do RDO
     * @param entity RDOEntity que será atualizado
     * @return RDOEntity atualizado com os dados do record
     **/
    public RDOEntity applyUpdate(RDORecord record, RDOEntity entity, SimpleUser user) {

        entity.setClientEmployee(record.dadosCliente());
        entity.setContractorServices(record.servicosRealizadosContratada());
        entity.setContractorObservations(record.registrosObservacao());
        entity.setOvertimeJustification(record.justificativaHoraExtra());


        entity.setEmployeeDepartures(
                Optional.ofNullable(record.trabalhoDesembarque())
                        .orElse(List.of())
                        .stream()
                        .map(RDOEmployeeDeparture::fromDTO)
                        .toList()
        );
        entity.setCraneControl(
                Optional.ofNullable(record.controleGuindaste())
                        .orElse(List.of())
                        .stream()
                        .map(CraneControl::fromDTO)
                        .toList()
        );

        entity.setCableControl(
                Optional.ofNullable(record.controleCaboAco())
                        .orElse(List.of())
                        .stream()
                        .map(SteelCableControl::fromDTO)
                        .toList()
        );

        entity.setLines(
                Optional.ofNullable(record.linhasRDO())
                        .orElse(List.of())
                        .stream()
                        .map(RDOLineRequest::toEntity)
                        .toList()
        );

        entity.setEquipments(
                Optional.ofNullable(record.equipamentos())
                        .orElse(List.of())
                        .stream()
                        .map(RDOEquipment::fromDTO)
                        .toList()
        );


        entity.setSteelCable(Optional.ofNullable(record.cabosDeAcos()).orElse(List.of()));
        entity.setAccessoryKits(Optional.ofNullable(record.kitsAcessorios()).orElse(List.of()));


        if (record.servicos() != null) {
            List<RDOServiceEntity> updatedServices = record.servicos().stream()
                    .filter(svcDto -> svcDto.presencaColaboradores() != null)
                    .flatMap(svcDto ->
                            svcDto.presencaColaboradores().stream()
                                    .map(emp -> {
                                        RDOServiceEntity ent = RDOServiceEntity.fromDTO(svcDto, emp);
                                        ent.setMergeKey(svcDto.id() + ":" + emp.matricula());
                                        return ent;
                                    })
                    ).toList();
            entity.setServices(updatedServices);
        }
        if(entity.getLoggers() == null || entity.getLoggers().isEmpty()){
            entity.setLoggers(List.of(createLogger(
                    userMapper.toLogger(user),
                    record.justificativa(),
                    RDOLoggerType.EDIT
            )));
        } else {
            entity.getLoggers().add(
                    createLogger(
                            userMapper.toLogger(user),
                            record.justificativa(),
                            RDOLoggerType.EDIT
                    ));
        }

        return entity;
    }

    private static RDOLogger createLogger(RDOLoggerUser user, String justification, RDOLoggerType action) {
        final String sector = "OP";
        return RDOLogger.builder()
                .wasAnalyzed(true)
                .isInfoCorrect(true)
                .action(action)
                .justification(justification)
                .date(LocalDateTime.now())
                .sector(sector)
                .user(user)
                .build();
    }
}
