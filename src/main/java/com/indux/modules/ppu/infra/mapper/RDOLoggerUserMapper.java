package com.indux.modules.ppu.infra.mapper;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RDOLoggerUserMapper {

    @Mapping(source = "nome", target = "name")
    RDOLoggerUser toLogger(SimpleUser entity);

    @Mapping(source = "employee.name", target = "name")
    @Mapping(source = "id", target = "id")
    RDOLoggerUser toLogger(EmployeeDTO employee, UUID id);
}


