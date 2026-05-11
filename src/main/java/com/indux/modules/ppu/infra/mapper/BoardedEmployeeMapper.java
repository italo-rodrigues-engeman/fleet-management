package com.indux.modules.ppu.infra.mapper;

import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.core.domain.model.employee.Employee;
import com.indux.modules.ppu.domain.entities.rdo.RDOEmployeeDeparture;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import com.indux.modules.ppu.infra.mio.dto.MioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BoardedEmployeeMapper {
    BoardedEmployeeMapper INSTANCE = Mappers.getMapper(BoardedEmployeeMapper.class);

    BoardedEmployee fromEmployee(Employee employee);

    BoardedEmployee fromSimpleEmployeeDTO(SimpleEmployeeDTO dto);
    List<BoardedEmployee> fromSimpleEmployeeDTOList(List<SimpleEmployeeDTO> dtos);

    @Mapping(source = "service.registration",      target = "registration")
    @Mapping(source = "service.name",          target = "name")
    @Mapping(source = "service.cargoID",       target = "position")
    @Mapping(source = "service.cargoNome",     target = "positionName")
    @Mapping(source = "service.statusEmployee", target = "status")
    @Mapping(target = "rdoId",                 expression = "java(id)")
    @Mapping(target = "platform",              expression = "java(platform)")
    BoardedEmployee fromRDOService(RDOServiceEntity service, String platform, String id);

    default List<BoardedEmployee> fromRDOServiceList(List<RDOServiceEntity> services, String platform, String id) {
        if (services == null || services.isEmpty()) return Collections.emptyList();
        List<BoardedEmployee> out = new ArrayList<>(services.size());
        for (RDOServiceEntity s : services) {
            out.add(fromRDOService(s, platform, id));
        }
        return out;
    }

    @Mapping(target = "status", constant = "Desembarque")
    @Mapping(target = "rdoId", expression = "java(id)")
    @Mapping(target = "platform", expression = "java(platform)")
    BoardedEmployee fromRDOEmployeeDeparture(RDOEmployeeDeparture departure, String platform, String id);

    default List<BoardedEmployee> fromRDOEmployeeDepartureList(List<RDOEmployeeDeparture> departures, String platform, String id) {
        if (departures == null || departures.isEmpty()) return Collections.emptyList();
        List<BoardedEmployee> out = new ArrayList<>(departures.size());
        for (RDOEmployeeDeparture d : departures) {
            out.add(fromRDOEmployeeDeparture(d, platform, id));
        }
        return out;
    }


    @Mapping(target = "boarding",        source = "ev.dataInicio")
    @Mapping(target = "landingForecast", source = "ev.dataFim")
    @Mapping(target = "status",          source = "ev.status")
    @Mapping(target = "registration",    source = "base.registration")
    @Mapping(target = "name",            source = "base.name")
    @Mapping(target = "position",        source = "base.position")
    @Mapping(target = "positionName",    source = "base.positionName")
    @Mapping(target = "boardingForecast",source = "base.boardingForecast")
    @Mapping(target = "sispat",          source = "base.sispat")
    @Mapping(target = "contract",        source = "base.contract")
    @Mapping(target = "rdoId",           source = "base.rdoId")
    @Mapping(target = "filial_HCM",      source = "base.filial_HCM")
    BoardedEmployee fromBaseAndEvent(BoardedEmployee base, MioResponse ev);

    @Mapping(source = "matricula", target = "registration")
    @Mapping(source = "nome", target = "name")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "dataInicio", target = "boarding")
    @Mapping(source = "dataFim", target= "landingForecast")
    BoardedEmployee fromMioResponse(MioResponse response);

    List<BoardedEmployee> fromMioResponseList(List<MioResponse> responses);
}
